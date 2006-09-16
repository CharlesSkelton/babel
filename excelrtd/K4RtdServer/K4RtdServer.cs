/*************************************************************
 * Title:   Excel RTD Server for kdb+
 * Date:    6th November 2004
 * Author:  Charlie Skelton, http://www.skelton.de
 * License: Distributed under the license found at 
 *          http://creativecommons.org/licenses/by-sa/2.0/
 * Version  1.0
 * Thanks to Arthur and Simon for making this possible
*************************************************************/

/* I recommend reading 
   http://msdn.microsoft.com/library/default.asp?url=/library/en-us/dnexcl2k2/html/odc_xlrtdbuild.asp
   before making any changes to this code
*/

using Microsoft.Office.Interop.Excel;
using System;
using System.IO;
using System.Collections;
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Threading;
using System.Timers;
using System.Configuration;
using System.ComponentModel;
using System.Reflection;
using System.Text.RegularExpressions;
using System.Collections.Specialized;
using System.Security;
using System.Xml;


namespace K4RtdServer
{
	[GuidAttribute("5803D656-B807-4ecc-A693-E09847577BFD"),	ProgId("K4RtdServer")]

	public class K4RtdServer : IRtdServer
	{
		private IRTDUpdateEvent xlRTDUpdate;

        private TraceSwitch     traceSwitch= null;

		private Hashtable       tables= new Hashtable();
		private Hashtable       cache= new Hashtable();

		private string          plantName="";
		private string          host="";
		private short           port=0;
		private string          username="";
		private string          password="";
		private c               conn;

		private Thread          thread;
		private Hashtable       lastSubs= new Hashtable();
		private bool            fill= false;
		System.Timers.Timer     timer=new System.Timers.Timer();
	
		// This function reads messages from the ticker plant
	    void upd()
		{
			bool exit= false;

			Trace.WriteLineIf( traceSwitch.TraceVerbose, "Reader thread started...");

			while( ! exit)
			{
				try
				{
					bool notify= false;

					// read message from ticker plant
					object r=conn.k();

					lock( this)
					{
						// check that msg is of expected format
						if( r is object [])
						{
							object [] objs= (object []) r;

							if( (objs.Length == 3) 
								&&  (objs[0] is string)
								&&  (objs[1] is string)
								&&  ((objs[2] is c.Flip) || (objs[2] is c.Dict)))
							{
								if(objs[2] is c.Dict)
								{
									objs[2]= c.td( objs[2]);
								}

								// we only accept upd fn call
								if( "upd".Equals((string)objs[0]))
								{
									// get the table being updated
									string table= (string) objs[1];

									c.Flip flip= (c.Flip) objs[2];

									string [] columns= flip.x;
									object [] data=    flip.y;
										
									// go through columns looking for sym column
									for( int i=0; i < columns.Length; i++)
									{
										if( "sym".Equals( columns[i]))
										{
											string [] symbols= (string []) data[i];

											for( int j=0; j < columns.Length; j ++)
											{
												if( i != j) // skip "sym" column
												{
													string column= columns[j];

													for( int k= 0; k < symbols.Length; k ++)
													{			
														string symbol= symbols[k];
														string key= table+column+symbol;

														// see if we are monitoring for this table/sym/column
														RTDData curItem = (RTDData)cache[key];

														if( curItem != null)
														{
															// get the new value for this sym
															Array a= (Array) data[j];
															Object o= a.GetValue(k);
															bool isNull= false;

															// if we are filling then check for nulls
															if( fill)
															{
																if( o is double)
																{
																	if( Double.IsNaN((double)o))
																	{
																		isNull= true;
																	}
																}
																else if( o is float)
																{
																	if( Single.IsNaN((float)o))
																	{
																		isNull= true;
																	}
																}
																else if( o is int)
																{
																	if( Int32.MinValue == (int)o)
																	{
																		isNull= true;
																	}
																}
																else if( o is short)
																{
																	if( Int16.MinValue == (short)o)
																	{
																		isNull= true;
																	}
																}
																else if( o is long)
																{
																	if( Int64.MinValue == (long)o)
																	{
																		isNull= true;
																	}
																}
																else if( o is string)
																{
																	if( ((string)o).Length == 0)
																	{
																		isNull= true;
																	}
																}
															}

															if( ! isNull)
															{
																// store the new value
																curItem.setVal(o);
																// flag that we need to notify excel later
																notify= true;
															}
														}
													}
												}
											}
										}
									}
								}
							}
							else 
							{
								Trace.WriteLineIf( traceSwitch.TraceWarning, "Received unrecognised function call from plant: " + objs[0]);
							}
						}

						if( notify)
						{
							Trace.WriteLineIf( traceSwitch.TraceVerbose, "Update received");

							if( xlRTDUpdate != null)
							{
								// tell excel to come and get the data
								xlRTDUpdate.UpdateNotify();
							}
						}

						Trace.Flush();
					}
				}
				catch( Exception e)
				{
					Trace.WriteLineIf( traceSwitch.TraceError, "Exception whilst receiving update: " + e.ToString());

					lock( this)
					{
						if( conn != null)
						{
							try
							{
								conn.Close();
							}
							catch(Exception)
							{
							}

							conn= null;

							exit= true;

							if( xlRTDUpdate != null)
							{
								xlRTDUpdate.Disconnect();
							}
						}
					}
				}
			}

			Trace.WriteLineIf( traceSwitch.TraceVerbose, "Connection closed, exiting reader thread");
		}

		public void connect()
		{
			lock( this)
			{
				if( conn == null)
				{
					try
					{
						conn=new c(host,port, username+":"+password);

						// get a list of table names from the ticker plant
						String [] names= (String[]) conn.k( "tables`.");

						for( int i=0; i<names.Length; i++)
						{
							tables[names[i]]= new ArrayList();
						}

						// start listening for updates
						thread= new Thread(new ThreadStart(upd));

						thread.Start();
					}
					catch( Exception e)
					{
                        Trace.WriteLineIf( traceSwitch.TraceError, "Cannot connect to ["+host+":"+port+"]");

						if( xlRTDUpdate != null)
						{
							xlRTDUpdate.Disconnect();
						}

						throw e;
					}
				}
			}
		}

		public K4RtdServer() 
		{
			// we use a timer to allow excel a grace period to push all subscription updates through
			// when the timer expires we send on the subscriptions upstream to the ticker plant
			timer.AutoReset= false;
			timer.Elapsed+=new System.Timers.ElapsedEventHandler(Timer_Tick);
			timer.Interval=2000;

			traceSwitch= new TraceSwitch( "TraceLevelSwitch", "Trace Level for Entire Application");

            string s= DllConfigurationSettings.AppSettings["trace"];
			s=s.ToLower();
			if( "off".Equals(s))
			{
				traceSwitch.Level= TraceLevel.Off;
			}
			else if( "warning".Equals(s))
			{
				traceSwitch.Level= TraceLevel.Warning;
			}
			else if( "info".Equals(s))
			{
				traceSwitch.Level= TraceLevel.Info;
			}
			else if( "verbose".Equals(s))
			{
				traceSwitch.Level= TraceLevel.Verbose;
			}
			else if( "error".Equals(s))
			{
				traceSwitch.Level= TraceLevel.Error;
			}
			else 
			{
				traceSwitch.Level= TraceLevel.Error;
			}

            string logdir= DllConfigurationSettings.AppSettings["logdir"];

			logdir+= "/"+DateTime.Now.ToString("yyyyMMdd");

			if( !Directory.Exists( logdir))
			{
				Directory.CreateDirectory( logdir);
			}

			string logFile= logdir+"/log_"+DateTime.Now.ToString("HHmmssfff")+".txt";

			try
			{
				Trace.Listeners.Clear();
				Trace.Listeners.Add(new K4TraceListener(logFile)); 

				Trace.WriteLineIf( traceSwitch.TraceInfo, "Creating K4RtdServer");
			}
			catch( Exception e)
			{
				Debug.WriteLine(e.ToString());
			}

			Assembly assembly = Assembly.GetExecutingAssembly();
			AssemblyName an = assembly.GetName();
			Version version= an.Version;
			Trace.WriteLine( "Version " + version.Major + "." + version.Minor  + "." + version.Build + "." + version.Revision);

			// read config details from config file
			plantName= DllConfigurationSettings.AppSettings["name"];
			host=      DllConfigurationSettings.AppSettings["host"];
			port=      short.Parse( DllConfigurationSettings.AppSettings["port"]);
			username=  DllConfigurationSettings.AppSettings["username"];
			password=  DllConfigurationSettings.AppSettings["password"];
            fill=      bool.Parse( DllConfigurationSettings.AppSettings["fill"]);

            Trace.WriteLineIf( traceSwitch.TraceVerbose, "Plant ["+plantName+"] is ["+host+":"+port+"]");
		}

		#region IRtdServer Members

		public void ServerTerminate()
		{
			lock( this)
			{
				Trace.WriteLineIf( traceSwitch.TraceInfo, "Terminating K4RtdServer");
				xlRTDUpdate = null;
			}
		}

		public int Heartbeat()
		{
            Trace.WriteLineIf( traceSwitch.TraceInfo, "Heartbeat");

			// if disconnected let excel know the server is dead
			if( conn == null)
			{
				return 0;
			}
			else 
			{
				return 1;
			}
		}

		public void DisconnectData(int topic)
		{
            Trace.WriteLineIf( traceSwitch.TraceInfo, "Disconnecting topic: " + topic);

			lock( this)
			{
				foreach (DictionaryEntry de in cache)
				{
					RTDData data= (RTDData) de.Value; 
			
					if (data.containsTopic( topic))
					{
						data.removeTopic(topic);
					}

					if( data.getTopics().Length == 0)
					{
						cache.Remove( de.Key);
					}
				}
			}

			timer.Stop();
			timer.Start();
		}

		public Array RefreshData(ref int TopicCount)
		{
            Trace.WriteLineIf( traceSwitch.TraceInfo, "RefreshData");

			lock( this)
			{
				int numItems= 0;

				foreach( DictionaryEntry de in cache)
				{
					RTDData item = (RTDData) de.Value;

					if( item.Updated)
					{
						numItems+= item.getTopics().Length;
					}
				}

				object[,] ret = new object[2,numItems]; 

				int i= 0;

				try
				{
					foreach( DictionaryEntry de in cache)
					{
						RTDData item = (RTDData) de.Value;

						if( item.Updated)
						{
							int [] topics= item.getTopics();

							for( int j= 0; j < topics.Length; j++)
							{
								ret[0, i] = topics[j];
								ret[1, i] = item.getVal(topics[j]);
								i++;
							}
						}
					}
				}
				catch( Exception e)
				{
					Debug.WriteLine(e.ToString());
				}

				TopicCount= i;

				Trace.Flush();

				return ret;
			}
		}

		public int ServerStart(IRTDUpdateEvent CallbackObject)
		{
			lock( this)
			{
				Trace.WriteLineIf( traceSwitch.TraceInfo, "ServerStart");
				xlRTDUpdate = CallbackObject;
			}

			return 1;
		}

		private void subscribe()
		{
			// connect to ticker plant
			connect();

			// buid a list of tables and symbols that we want to subscribe to
			lock( this)
			{
				foreach( DictionaryEntry de in cache)
				{
					RTDData item = (RTDData) de.Value;

					ArrayList symbols= (ArrayList) tables[item.Table];

					if( symbols == null)
					{
						symbols= new ArrayList();
					}

					if( ! symbols.Contains( item.Symbol))
					{
						symbols.Add( item.Symbol);
					}
				}

				foreach( DictionaryEntry de in tables)
				{
					String table= "`" + (String) de.Key;
					ArrayList symbols= (ArrayList) de.Value;

					string [] ss=(string[]) symbols.ToArray( typeof(String) );
					Array.Sort( ss);

					string s="";

					if( ss.Length >0)
					{
						s+="`$(";

						for( int i=0;i<ss.Length;i++)
						{
							if( i > 0)
							{
								s+=";";
							}

							s+= "\""+ss[i]+"\"";
						}

						s+=")";
					}
					else 
					{
						s= "()";
					}

					string substring= ".u.sub["+table+";"+s+"]";

					// only send a new request if different from the last request
					if( !substring.Equals( lastSubs[table]))
					{
						Trace.WriteLineIf( traceSwitch.TraceInfo, "subscribing " + substring);
						conn.ks(substring);
						lastSubs[table]= substring;
					}					
				}
			}
		}

		public void Timer_Tick(object sender,System.Timers.ElapsedEventArgs eArgs)
		{
			lock( this)
			{
				// timer has expired, that means no we have received the complete subscription list
				// from excel. We can now subscribe to the ticker plant
				subscribe();
			}
		}
  

		public Object ConnectData(int topic, ref Array Strings, ref bool GetNewValues)
		{
			lock( this)
			{
				string table, column, symbol;
				int    index=0;
				RTDData data= null;

				GetNewValues = true;

				try
				{
					string plant= Strings.GetValue(0).ToString().Trim();

					if( plantName.Equals( plant))
					{
						table  = Strings.GetValue(1).ToString().Trim();
						column = Strings.GetValue(2).ToString().Trim();
						symbol = Strings.GetValue(3).ToString().Trim();

						if( Strings.Length > 4)
						{
							index= Int32.Parse(Strings.GetValue(4).ToString().Trim());
						}

						Trace.WriteLineIf( traceSwitch.TraceInfo, "ConnectData " + topic + "," + table + ":" + column + ":" + symbol);

						string key= table+column+symbol;

						data= (RTDData) cache[key];

						if( data == null)
						{						
							data= new RTDData(table,column,symbol);
							cache.Add(key, data);
						}

						if( !data.containsTopic( topic))
						{
							data.addTopic( topic, index);
						}

						timer.Stop();
						timer.Start();
					
						return (data.getVal(topic));
					}
					else 
					{
                        Trace.WriteLineIf( traceSwitch.TraceWarning, "Plant name " + plant + " is not recognised");
					}
				}
				catch (Exception e)
				{
                    Trace.WriteLineIf( traceSwitch.TraceInfo, "Exception: "+e.ToString());
				}

				return null;
			}
		}
		#endregion // IRtdServer Members
	
		private class RTDData
		{
			private bool    updated= false;
			private string	table;
			private string	column;
			private string	symbol;
			private int     history; // amount of history to store
			private ArrayList values= new ArrayList();
			private Hashtable topics= new Hashtable();
		
			public RTDData(string table, string column, string symbol)
			{
				this.table=  table;
				this.column= column;
				this.symbol= symbol;
			}

			public string Key
			{
				get
				{
					return table+column+symbol;
				}
			}

			public bool Updated
			{
				get
				{
					return updated;
				}
				set
				{
					updated= value;
				}
			}
		
			public string Table
			{
				get
				{
					return table;
				}
			}

			
			public string Symbol
			{
				get
				{
					return symbol;
				}
			}

			public object getVal(int topic)
			{
				lock( values)
				{
					if( topics.ContainsKey( topic))
					{
						int index= (int)topics[topic];

						if( values.Count > index)
						{
							updated= false;
							return values[index];
						}
					}
				}

				return null;
			}

			public void setVal(object val)
			{
				lock( values)
				{
					if( history >=1)
					{
						while( values.Count > history)
						{
							values.RemoveAt(values.Count-1);
						}
					}

					if( val is TimeSpan)
					{
						val= ((double)((TimeSpan)val).Ticks)/TimeSpan.TicksPerDay;
					}
                    //Dates are stored as the number of days since the beginning of the last century (i.e. 1-Jan-1900 is day 1). 

					values.Insert( 0, val);

					updated= true;
				}
			}

			public bool containsTopic( int topic)
			{
				return topics[topic] != null;
			}

			public int [] getTopics()
			{
				int [] results= new int[ topics.Count];

				int j=0;

				foreach( DictionaryEntry de in topics)
				{
					results[j++]= (int) de.Key;
				}

			    return results;
			}

			public void addTopic( int topic, int offset)
			{
				if( (offset+1) > history)
				{
					history= offset;
				}

				topics[topic]= offset;
			}

			public void removeTopic( int topic)
			{
				topics[topic]= null;
			}
		}
	}

	public abstract class DllConfigurationSettings
	{
		private static Hashtable dllSettings = new Hashtable();

		public static NameValueCollection AppSettings
		{
			get
			{
				string configFile = null; 

				Exception ex = null;

				try
				{
					Assembly callingAssembly = Assembly.GetCallingAssembly();
					FileInfo assemblyPath = new FileInfo(callingAssembly.Location);
					
					configFile = string.Concat( callingAssembly.GlobalAssemblyCache ? AppDomain.CurrentDomain.BaseDirectory : assemblyPath.Directory.ToString() + "\\",
						assemblyPath.Name, 
						".config");
				}
				catch (ArgumentException e){ex=e;}
				catch (SecurityException e){ex=e;}
				catch (UnauthorizedAccessException e){ex=e;}
				catch (PathTooLongException e){ex=e;}
				catch (NotSupportedException e){ex=e;}

				if (ex != null)
					throw new ConfigurationException("Unable to identify calling assembly file path and/or base directory of current application domain", ex);

				if (!File.Exists(configFile))
					throw new FileNotFoundException("Could not find or open config file", configFile);

				NameValueCollection settings = (dllSettings[configFile] as NameValueCollection);

				if (settings == null)
				{
					ReadXML(configFile, out settings);
					dllSettings[configFile] = settings;
				}

				return settings;
			}
		}
		
		private static void ReadXML(string configFile, out NameValueCollection settings)
		{
			settings = new NameValueCollection();

			XmlTextReader reader = null;
			try
			{
				reader = new XmlTextReader(configFile);
				reader.WhitespaceHandling = WhitespaceHandling.None;

				reader.MoveToContent();
				reader.ReadStartElement("configuration");
				reader.ReadStartElement("appSettings");
				
				while (reader.LocalName == "add")
				{
					string key = reader.GetAttribute("key");

					if (key == null)
						throw new ConfigurationException("'key' attribute value must be present");
					
					settings.Add(key, reader.GetAttribute("value"));	
					reader.Read();
				}
			}
			catch(XmlException xmlEx)
			{
				throw new ConfigurationException(string.Concat("Error parsing file: ",configFile," on line #",reader.LineNumber,"."), xmlEx, configFile, reader.LineNumber);
			}
			finally
			{
				if (reader != null) 
					reader.Close();
			}
		}
	}

	public class K4TraceListener : TextWriterTraceListener
	{
		private bool sol= true; // start of line

		public K4TraceListener():base()
		{
		}

		public K4TraceListener(String filename):base(filename)
		{
		}

		private void writeTimestamp()
		{
			base.Write( DateTime.Now.ToString( "HH:mm:ss.fff -> "));
		}

		override public void Write(string message) 
		{
			if(sol)
			{
				writeTimestamp();
				sol= false;
			}

			base.Write( message);
		}

		override public void WriteLine(string message) 
		{     
			writeTimestamp();
			base.WriteLine( message);
			sol= false;
		}
	}
}
