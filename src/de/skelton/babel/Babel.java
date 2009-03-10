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
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;
import kx.c;

public class Babel
{
    private static final String version="Babel for kdb+ Version 1.0\n";
    private static Map typeMap=new HashMap();

    private static void init() throws ClassNotFoundException
    {
        Class.forName("org.relique.jdbc.csv.CsvDriver");
        Class.forName("org.hsqldb.jdbcDriver");
        Class.forName("com.mysql.jdbc.Driver");
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        typeMap.put(new Integer(java.sql.Types.BOOLEAN),boolean.class);
        typeMap.put(new Integer(java.sql.Types.SMALLINT),short.class);
        typeMap.put(new Integer(java.sql.Types.BIGINT),long.class);
        typeMap.put(new Integer(java.sql.Types.REAL),float.class);
        typeMap.put(new Integer(java.sql.Types.DOUBLE),double.class);
        typeMap.put(new Integer(java.sql.Types.DATE),java.sql.Date.class);
        typeMap.put(new Integer(java.sql.Types.TIME),java.sql.Time.class);
        typeMap.put(new Integer(java.sql.Types.TIMESTAMP),java.sql.Timestamp.class);
        typeMap.put(new Integer(java.sql.Types.VARCHAR),String.class);
        typeMap.put(new Integer(java.sql.Types.INTEGER),int.class);
    }
    
    public static String[] getAvailableIDs()
    {
        return TimeZone.getAvailableIDs();
    }

    public static c.Flip query(char[] _connectionDetails,char[] _query) throws ClassNotFoundException,SQLException
    {
        String connectionDetails=new String(_connectionDetails);
        String query=new String(_query);
        String[] columnNames=new String[0];
        SmartArray[] data=new SmartArray[0];

        Connection conn=null;
        try
        {
            conn=DriverManager.getConnection(connectionDetails);
            Statement stmt=null;
            try
            {
                stmt=conn.createStatement();
                ResultSet results=null;
                try
                {
                    results=stmt.executeQuery(query);
                    ResultSetMetaData rmd=results.getMetaData();

                    int nCols=rmd.getColumnCount();
                    columnNames=new String[nCols];
                    data=new SmartArray[nCols];
                    for(int col=0;col<nCols;col++)
                    {
                        columnNames[col]=rmd.getColumnName(col+1);
                        int dataType=rmd.getColumnType(col+1);
                        Class clazz=(Class)typeMap.get(new Integer(dataType));
                        if(clazz==null)
                            throw new RuntimeException("Unsupported sql data type: "+dataType);
                        data[col]=new SmartArray(clazz);
                    }

                    while(results.next())
                    {
                        for(int col=0;col<nCols;col++)
                        {
                            int dataType=rmd.getColumnType(col+1);
                            switch(dataType)
                            {
                                case (java.sql.Types.BOOLEAN):
                                    {
                                        boolean b=results.getBoolean(col+1);
                                        if(results.wasNull())
                                            b=false;
                                        data[col].append(b);
                                    }
                                    break;
                                case (java.sql.Types.DOUBLE):
                                    {
                                        double d=results.getDouble(col+1);
                                        if(results.wasNull())
                                            d=Double.NaN;
                                        data[col].append(d);
                                    }
                                    break;
                                case (java.sql.Types.REAL):
                                    {
                                        float f=results.getFloat(col+1);
                                        if(results.wasNull())
                                            f=Float.NaN;
                                        data[col].append(f);
                                    }
                                    break;
                                case (java.sql.Types.SMALLINT):
                                    {
                                        short s=results.getShort(col+1);
                                        if(results.wasNull())
                                            s=Short.MIN_VALUE;
                                        data[col].append(s);
                                    }
                                    break;
                                case (java.sql.Types.BIGINT):
                                    {
                                        long l=results.getLong(col+1);
                                        if(results.wasNull())
                                            l=Long.MIN_VALUE;
                                        data[col].append(l);
                                    }
                                    break;
                                case (java.sql.Types.VARCHAR):
                                    {
                                        String s=results.getString(col+1);
                                        if(results.wasNull())
                                            s="";
                                        data[col].append(s);
                                    }
                                    break;
                                case (java.sql.Types.INTEGER):
                                    {
                                        int i=results.getInt(col+1);
                                        if(results.wasNull())
                                            i=Integer.MIN_VALUE;
                                        data[col].append(i);
                                    }
                                    break;
                                case (java.sql.Types.DATE):
                                    {
                                        Date d=results.getDate(col+1);
                                        if(results.wasNull())
                                            d=(Date)c.NULL('d');
                                        data[col].append(d);
                                    }
                                    break;
                                case (java.sql.Types.TIME):
                                    {
                                        Time t=results.getTime(col+1);
                                        if(results.wasNull())
                                            t=(Time)c.NULL('t');
                                        data[col].append(t);
                                    }
                                    break;
                                case (java.sql.Types.TIMESTAMP):
                                    {
                                        Timestamp t=results.getTimestamp(col+1);
                                        if(results.wasNull())
                                            t=(Timestamp)c.NULL('z');
                                        data[col].append(t);
                                    }
                                    break;
                                default:
                                {
                                    throw new RuntimeException("Unsupported sql data type: "+dataType);
                                }
                            }
                        }
                    }
                }
                finally
                {
                    if(results!=null)
                        results.close();
                }
            }
            finally
            {
                if(stmt!=null)
                    stmt.close();
            }
        }
        finally
        {
            if(conn!=null)
                conn.close();
        }

        Object[] o=new Object[data.length];
        for(int i=0;i<o.length;i++)
            o[i]=data[i].toArray();
        return new c.Flip(new c.Dict(columnNames,o));
    }

    public static int update(char[] _connectionDetails,char[] _expression) throws SQLException,ClassNotFoundException
    {
        String connectionDetails=new String(_connectionDetails);
        String expression=new String(_expression);

        Connection conn=null;
        try
        {
            conn=DriverManager.getConnection(connectionDetails);
            Statement stmt=null;
            try
            {
                stmt=conn.createStatement();

                int i=stmt.executeUpdate(expression);
            }
            finally
            {
                if(stmt!=null)
                {
                    stmt.close();
                }
            }
        }
        finally
        {
            if(conn!=null)
            {
                conn.close();
            }
        }
        return 1;
    }

    public static void main(String[] args)
    {
        System.out.println(version);

        int port=9999;

        if(args.length>0)
        {
            try
            {
                port=Integer.parseInt(args[0]);
            }
            catch(Exception e)
            {
                System.err.println("Error: could not parse port number argument. Exiting.");
                System.exit(1);
            }
        }
        final int _port=port;
        try
        {
            init();
        }
        catch(ClassNotFoundException ex)
        {
            ex.printStackTrace();
            System.exit(1);
        }

        Runnable runner=new Runnable()
        {
            public void run()
            {
                try
                {
                    final ServerSocket ss=new ServerSocket(_port);
                    Logger.log("Listening on port "+_port+" for connections...");

                    while(true)
                    {
                        final c c=new c(ss);
                        Logger.log("New connection from "+c.s.getRemoteSocketAddress());
                        try
                        {
                            Runnable runner=new Runnable()
                            {
                                public void run()
                                {
                                    try
                                    {
                                        while(true)
                                        {
                                            Object obj=c.k();
                                            Logger.log("Request from "+c.s.getRemoteSocketAddress());
                                            if((obj instanceof Object[])&&(((Object[])obj).length>=1)&&(((Object[])obj)[0] instanceof char[]))
                                            {
                                                String methodName=new String((char[])((Object[])obj)[0]);

                                                try
                                                {
                                                    Class cls=Babel.class;
                                                    int splitAt=methodName.lastIndexOf('.');
                                                    if(splitAt != -1)
                                                    {
                                                        String className= methodName.substring(0,splitAt);
                                                        cls= Class.forName(className);
                                                        methodName= methodName.substring(splitAt+1,methodName.length());
                                                    }
                                                    Vector v= new Vector();
                                                    for( int i= 1; i < ((Object[])obj).length; i++)
                                                        if(((Object[])obj)[i] != null)
                                                            v.add(((Object[])obj)[i]);
                                                    Object[] args=v.toArray();
                                                    Class[] argClasses=new Class[args.length];

                                                    for(int i=0;i<args.length;i++)
                                                    {
                                                        Class c= args[i].getClass();
                                                        if(c == Integer.class)
                                                            c= int.class;
                                                        argClasses[i]=c;
                                                    }

                                                    Method method=cls.getMethod(methodName,argClasses);
                                                    try
                                                    {
                                                        Object result=method.invoke(null,args);
                                                        c.w(2,result);
                                                        Logger.log("Response sent");
                                                    }
                                                    catch(IllegalAccessException e)
                                                    {
                                                        e.printStackTrace();
                                                        c.wError(2,e.getMessage());
                                                    }
                                                    catch(InvocationTargetException e)
                                                    {
                                                        e.printStackTrace();

                                                        c.wError(2,e.getCause().getMessage());
                                                    }
                                                }
                                                catch(NoSuchMethodException e)
                                                {
                                                    e.printStackTrace();
                                                    c.wError(2,e.getMessage());
                                                }

                                            // "jdbc:relique:csv:/Users/cskelton/q", 
                                            }

                                        }
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();

                                    }
                                    finally
                                    {
                                        if(c!=null)
                                        {
                                            try
                                            {
                                                c.close();
                                            }
                                            catch(IOException ex)
                                            {
                                            }
                                        }
                                    }
                                }
                            };

                            Thread t=new Thread(runner);
                            t.start();
                        }
                        catch(Exception e)
                        {
                        }
                    }
                }
                catch(Exception e)
                {
                    System.out.println("Error : "+e.getMessage());
                }
            }
        };
        Thread t=new Thread(runner);
        t.start();
    }
}