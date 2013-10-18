/* Babel for kdb+ by Charles Skelton
   is licensed under a Creative Commons Attribution-Noncommercial-Share Alike 3.0 Germany License
   http://creativecommons.org/licenses/by-nc-sa/3.0
*/
package de.skelton.babel;

import de.skelton.util.SmartArray;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import de.skelton.util.Logger;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import kx.c;

public class Babel{
  private static final String about="Babel for kdb+ v1.32 2013.10.18\n";
  private static Map typeMap=new HashMap();
  private static void init() throws ClassNotFoundException{
//    http://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/mapping.html
    typeMap.put(new Integer(java.sql.Types.BIGINT),long.class);
    typeMap.put(new Integer(java.sql.Types.BIT),boolean.class);
    typeMap.put(new Integer(java.sql.Types.BOOLEAN),boolean.class);
    typeMap.put(new Integer(java.sql.Types.CHAR),char[].class);
    typeMap.put(new Integer(java.sql.Types.DATE),java.sql.Date.class);
    typeMap.put(new Integer(java.sql.Types.DECIMAL),char[].class);    
    typeMap.put(new Integer(java.sql.Types.NUMERIC),char[].class);    
    typeMap.put(new Integer(java.sql.Types.DOUBLE),double.class);
    typeMap.put(new Integer(java.sql.Types.FLOAT),double.class);
    typeMap.put(new Integer(java.sql.Types.INTEGER),int.class);
    typeMap.put(new Integer(java.sql.Types.LONGVARBINARY),byte[].class);
    typeMap.put(new Integer(java.sql.Types.LONGVARCHAR),char[].class);
    typeMap.put(new Integer(java.sql.Types.REAL),float.class);
    typeMap.put(new Integer(java.sql.Types.SMALLINT),int.class);
    typeMap.put(new Integer(java.sql.Types.TIME),java.sql.Time.class);
    typeMap.put(new Integer(java.sql.Types.TIMESTAMP),java.sql.Timestamp.class);
    typeMap.put(new Integer(java.sql.Types.TINYINT),int.class);    
    typeMap.put(new Integer(java.sql.Types.VARCHAR),char[].class);    
  }
  
  public static Object query(char[] _connectionDetails,char[] _query) throws ClassNotFoundException,SQLException{
    String connectionDetails=new String(_connectionDetails);
    String query=new String(_query);
    String[] columnNames=new String[0];
    Connection conn=null;
    ArrayList v=new ArrayList();
    try{
      conn=DriverManager.getConnection(connectionDetails);
      PreparedStatement stmt=null;
      try{
        stmt=conn.prepareStatement(query);
        // oracle streams results by default anyway?
        // next 2 lines are mysql specific, to stream resultset on demand
        // stmt=conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
        // stmt.setFetchSize(Integer.MIN_VALUE);
        ResultSet results=null;
        try{
          boolean isResultSet=stmt.execute();
          while(true){
            if(isResultSet){
              results=stmt.getResultSet();
              ResultSetMetaData rmd=results.getMetaData();
              int nCols=rmd.getColumnCount();
              columnNames=new String[nCols];
              SmartArray[]data=new SmartArray[nCols];
              int[]dataTypes=new int[nCols];
              for(int col=0;col<nCols;col++){
                columnNames[col]=rmd.getColumnName(col+1);
                dataTypes[col]=rmd.getColumnType(col+1);
                if(dataTypes[col]==java.sql.Types.NUMERIC||dataTypes[col]==java.sql.Types.DECIMAL){
                  // NUMBER(precision,scale);(0,0)-unspecified;(38,0)-default
                  // oracle.jdbc.J2EE13Compliant=true eliminates getScale=-127
                  int scale=rmd.getScale(col+1);
                  int precision=rmd.getPrecision(col+1);
                  if(precision>0&&(scale==0||scale==-127)){
                    if(precision<=9)
                      dataTypes[col]=java.sql.Types.INTEGER;
                    else if(precision<=18)
                      dataTypes[col]=java.sql.Types.BIGINT;
                  }
                  else if(precision<=7)
                    dataTypes[col]=java.sql.Types.FLOAT;
                  else if(precision<=15)
                    dataTypes[col]=java.sql.Types.DOUBLE;
                }
                Class clazz=(Class)typeMap.get(new Integer(dataTypes[col]));
                if(clazz==null)
                  throw new RuntimeException("Unsupported sql data type: "+dataTypes[col]+" in column "+columnNames[col]);
                data[col]=new SmartArray(clazz);
              }
              rmd=null; 
              while(results.next()){
                for(int col=0;col<nCols;col++){
                  switch(dataTypes[col]){
                    case(java.sql.Types.BIT):
                    case(java.sql.Types.BOOLEAN):{boolean b=results.getBoolean(col+1);if(results.wasNull())b=false;data[col].add(b);}break;
                    case(java.sql.Types.FLOAT):
                    case(java.sql.Types.DOUBLE):{double d=results.getDouble(col+1);if(results.wasNull())d=Double.NaN;data[col].add(d);}break;
                    case(java.sql.Types.REAL):{float f=results.getFloat(col+1);if(results.wasNull())f=Float.NaN;data[col].add(f);}break;
                    case(java.sql.Types.TINYINT):
                    case(java.sql.Types.SMALLINT):{short h=results.getShort(col+1);if(results.wasNull())h=Short.MIN_VALUE;data[col].add(h);}break;
                    case(java.sql.Types.INTEGER):{int i=results.getInt(col+1);if(results.wasNull())i=Integer.MIN_VALUE;data[col].add(i);}break;
                    case(java.sql.Types.BIGINT):{long l=results.getLong(col+1);if(results.wasNull())l=Long.MIN_VALUE;data[col].add(l);}break;
                    case(java.sql.Types.LONGVARBINARY):{byte[] b=results.getBytes(col+1);if(results.wasNull())b=new byte[0];data[col].add(b);}break;
                    case(java.sql.Types.DECIMAL):
                    case(java.sql.Types.NUMERIC):
                    case(java.sql.Types.CHAR):
                    case(java.sql.Types.VARCHAR):
                    case(java.sql.Types.LONGVARCHAR):{String s=results.getString(col+1);if(results.wasNull())s="";data[col].add(s.toCharArray());}break;
                    case(java.sql.Types.DATE):{Date d=results.getDate(col+1);if(results.wasNull())d=(Date)c.NULL('d');data[col].add(d);}break;
                    case(java.sql.Types.TIME):{Time t=results.getTime(col+1);if(results.wasNull())t=(Time)c.NULL('t');data[col].add(t);}break;
                    case(java.sql.Types.TIMESTAMP):{Timestamp t=results.getTimestamp(col+1);if(results.wasNull())t=(Timestamp)c.NULL('p');data[col].add(t);}break;
                    default:{throw new RuntimeException("Unsupported sql data type: "+dataTypes[col]+" in column "+columnNames[col]);}
                  }
                }
              }
              Object[]o=new Object[data.length];
              for(int i=0;i<o.length;i++)
                o[i]=data[i].compact();
              data=null;
              v.add(new c.Flip(new c.Dict(columnNames,o)));
            }
            else if(-1==stmt.getUpdateCount())
              break;
            isResultSet=stmt.getMoreResults();
          }
        }
        finally{
          if(results!=null){results.close();results=null;}
        }
      }
      finally{
        if(stmt!=null){stmt.close();stmt=null;}
      }
    }
    finally{
      if(conn!=null){conn.close();conn=null;}
    }
    return v.size()==1?v.get(0):v.toArray();
  }

  public static int update(char[] _connectionDetails,char[] _expression) throws SQLException,ClassNotFoundException{
    String connectionDetails=new String(_connectionDetails);
    String expression=new String(_expression);
    Connection conn=null;
    try{
      conn=DriverManager.getConnection(connectionDetails);
      Statement stmt=null;
      try{
        stmt=conn.createStatement();
        int i=stmt.executeUpdate(expression);
      }
      finally{
        if(stmt!=null)
          stmt.close();
      }
    }
    finally{
      if(conn!=null)
        conn.close();
    }
    return 1;
  }

  public static void main(String[] args){
    System.out.println(about);
    int port=9999;
    if(args.length>0){
      try{
        port=Integer.parseInt(args[0]);
      }
      catch(Exception e){
        System.err.println("Error: could not parse port number argument. Exiting.");
        System.exit(1);
      }
    }
    
    if(args.length>1){
      for(int i=1; i<args.length; i++) {
        try{
            Class.forName(args[i]);
        }
        catch(Exception e){
            System.err.println("Error: could not load jdbc driver. Exiting.");
            System.exit(1);
        }
      }
    }
    
    final int _port=port;
    try{
      init();
    }
    catch(ClassNotFoundException ex){
      ex.printStackTrace();
      System.exit(1);
    }
    Runnable runner=new Runnable(){
      public void run(){
        try{
          java.net.SocketAddress socketAddress= new java.net.InetSocketAddress("127.0.0.1",_port);
	  ServerSocket ss= new ServerSocket();
	  ss.bind(socketAddress);
          Logger.log("Listening on localhost:"+_port+" for connections...");
          while(true){
            try{
              final c c=new c(ss,new c.IAuthenticate(){public boolean authenticate(String s){Logger.log("Authenticating "+s.split(":",2)[0]);return true;}});
              Logger.log("Accepted connection from "+c.s.getRemoteSocketAddress());
              Runnable runner=new Runnable(){
                public void run(){
                  try{
                    while(true){
                      Object obj=c.k();
                      Logger.log("Request from "+c.s.getRemoteSocketAddress());
                      if((obj instanceof Object[])&&(((Object[])obj).length>=1)&&(((Object[])obj)[0] instanceof char[])){
                        String methodName=new String((char[])((Object[])obj)[0]);
                        try{
                          Class cls=Babel.class;
                          int splitAt=methodName.lastIndexOf('.');
                          if(splitAt != -1){
                            String className= methodName.substring(0,splitAt);
                            cls= Class.forName(className);
                            methodName= methodName.substring(splitAt+1,methodName.length());
                          }
                          ArrayList v= new ArrayList();
                          for( int i=1;i<((Object[])obj).length; i++)
                            if(((Object[])obj)[i] != null)
                              v.add(((Object[])obj)[i]);
                          Object[] args=v.toArray();
                          Class[] argClasses=new Class[args.length];
                          for(int i=0;i<args.length;i++){
                            Class c= args[i].getClass();
                            if(c == Integer.class)
                              c= int.class;
                            argClasses[i]=c;
                          }
                          Method method=cls.getMethod(methodName,argClasses);
                          try{
                            Object result=method.invoke(null,args);
                            c.kr(result);
                            result=null;
                            Logger.log("Response sent");
                          }
                          catch(IllegalAccessException e){
                            e.printStackTrace();
                            c.ke(e.getMessage());
                          }
                          catch(InvocationTargetException e){
                            e.printStackTrace();
                            c.ke(e.getCause().getMessage());
                          }
                        }
                        catch(NoSuchMethodException e){
                          e.printStackTrace();
                          c.ke(e.getMessage());
                        }
                        finally{
                          Runtime.getRuntime().gc();                    
                        }
                      }
                      else
                        c.ke("Unrecognized query format. Expecting (\"query|update\";\"jdbc url\";\"query text\")");
                    }
                  }
                  catch(Exception e){
                    e.printStackTrace();
                  }
                  finally{
                    if(c!=null){
                      try{
                        c.close();
                      }
                      catch(IOException ex){
                      }
                    }
                    Runtime.getRuntime().gc();
                  }
                }
              };
              Thread t=new Thread(runner);
              t.start();
            }
            catch(Exception e){
                Logger.log(e);
            }
          }
        }
        catch(Exception e){
            Logger.log(e);
        }
      }
    };
    Thread t=new Thread(runner);
    t.start();
  }
}