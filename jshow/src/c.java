
// 2004.11.16 11..19/String[]..  c(socket)
// 2004.10.12 usr:pwd t(int[]) is .5 isarray is .1 lookup .05


import java.net.*;
import java.io.*;
import java.sql.*;
import java.text.*;

// javac c.java

public class c
{
    public static void main(String[] args)
    {
        try
        {
//c c=new c(new ServerSocket(5010));while(true)c.w(2,c.k());
//c c=new c("localhost",5010);String[]sym={"GE","MSFT"};Object[]x={sym,new double[2],new int[2]};
//c.ks("insert","trade",x);Object[]x={"GE",new Double(2.5),new Integer(23)};c.k(".u.upd","trade",x);
/*
c c=new c("localhost",5001);
  Object[]x={new Time(t()),"xx",new Double(93.5),new Integer(300)};
  Flip t=td(c.k("select sum size by sym from trade"));O(n(t.x));O(n(t.y[0]));O(at(t.y[0],0)); //cols rows data
 tm();for(int i=0;i<1;++i)c.ks("insert","trade",x);tm();
 c.close();
*/
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static SimpleDateFormat formatter  = new SimpleDateFormat();

    static
    {
        formatter.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
    }

    static synchronized String sd(String s, java.util.Date x)
    {
        formatter.applyPattern(s);
        return formatter.format(x);
    }

    public static String sd(Date x)
    {
        return sd("yyyy.MM.dd", x);
    }

    public static String sd(Time x)
    {
        return sd("HH:mm:ss.SSS", x);
    }

    public static String sd(Timestamp x)
    {
        return sd("yyyy.MM.dd HH:mm:ss.SSS", x);
    }

    DataInputStream i;
    OutputStream o;
    byte[] b,B;
    int j,J;
    boolean a;

    void io(Socket s) throws IOException
    {
        i = new DataInputStream(s.getInputStream());
        o = s.getOutputStream();
    }

    public void close()
    {
        if (i != null) try
        {
            i.close();
            o.close();
        }
        catch (IOException e)
        {
        }
        finally
        {
            i = null;
            o = null;
        }
    }

    public c(Socket s) throws IOException
    {
        io(s);
        i.read(b = new byte[99]);
        o.write(b, 0, 1);
    }

    public c(ServerSocket s) throws IOException
    {
        this(s.accept());
    }

    public c(String h, int p, String u) throws K4Exception, IOException
    {
        io(new Socket(h, p));
        B = new byte[1 + ns(u)];
        J = 0;
        w(u);
        o.write(B);
        if (1 != i.read(B, 0, 1)) throw new K4Exception("access");
    }

    public c(String h, int p) throws K4Exception, IOException
    {
        this(h, p, System.getProperty("user.name"));
    }

    public static class Dict
    {
        public Object x;
        public Object y;

        Dict(Object X, Object Y)
        {
            x = X;
            y = Y;
        }
    }

    public static class Flip
    {
        public String[] x;
        public Object[] y;

        Flip(Dict X)
        {
            x = (String[]) X.x;
            y = (Object[]) X.y;
        }
    }

    public static class Month
    {
        public int i;

        Month(int x)
        {
            i = x;
        }

        public String toString()
        {
            int m = i + 24000,y = m / 12;
            return i2(y / 100) + i2(y % 100) + "-" + i2(1 + m % 12);
        }
    }

    public static class KDate extends Date
    {
        public KDate(long date)
        {
            super(date);
        }

        public String toString()
        {
            return TypeFormatter.format(this);
        }
    }

    public class KTime extends Time
    {
        public KTime(long time)
        {
            super(time);
        }

        public String toString()
        {
            return TypeFormatter.format(this);
        }
    }

    public class KTimestamp extends Timestamp
    {
        public KTimestamp(long time)
        {
            super(time);
        }

        public String toString()
        {
            return TypeFormatter.format(this);
        }
    }



    public static class Minute
    {
        public int i;

        Minute(int x)
        {
            i = x;
        }

        public String toString()
        {
            return i2(i / 60) + ":" + i2(i % 60);
        }
    }

    public static class Second
    {
        public int i;

        Second(int x)
        {
            i = x;
        }

        public String toString()
        {
            return new Minute(i / 60).toString() + ':' + i2(i % 60);
        }
    }

    public static Flip td(Object X)
    {
        if (t(X) == 98) return (Flip) X;
        Dict d = (Dict) X;
        Flip a = (Flip) d.x,b = (Flip) d.y;
        int m = n(a.x),n = n(b.x);
        String[] x = new String[m + n];
        System.arraycopy(a.x, 0, x, 0, m);
        System.arraycopy(b.x, 0, x, m, n);
        Object[] y = new Object[m + n];
        System.arraycopy(a.y, 0, y, 0, m);
        System.arraycopy(b.y, 0, y, m, n);
        return new Flip(new Dict(x, y));
    }

    static String i2(int i)
    {
        return new java.text.DecimalFormat("00").format(i);
    }

    static void O(Object x)
    {
        System.out.println(x);
    }

    static void O(int x)
    {
        System.out.println(x);
    }

    static void O(boolean x)
    {
        System.out.println(x);
    }

    static void O(long x)
    {
        System.out.println(x);
    }

    static void O(double x)
    {
        System.out.println(x);
    }

    static long t()
    {
        return System.currentTimeMillis() % 86400000;
    }

    static long t;

    static void tm()
    {
        long u = t;
        t = t();
        if (u > 0) O(t - u);
    }

//object.getClass().isArray()
    static int t(Object x)
    {
        return x instanceof Boolean ? -1 : x instanceof Byte ? -4 : x instanceof Short ? -5 : x instanceof Integer ? -6 : x instanceof Long ? -7 :
                x instanceof Float ? -8 : x instanceof Double ? -9 : x instanceof Character ? -10 : x instanceof String ? -11 : x instanceof Month ? -13 :
                x instanceof Date ? -14 : x instanceof Timestamp ? -15 : x instanceof Minute ? -17 : x instanceof Second ? -18 : x instanceof Time ? -19 :
                x instanceof boolean[] ? 1 : x instanceof byte[] ? 4 : x instanceof short[] ? 5 : x instanceof int[] ? 6 : x instanceof long[] ? 7 :
                x instanceof float[] ? 8 : x instanceof double[] ? 9 : x instanceof char[] ? 10 : x instanceof String[] ? 11 : x instanceof Month[] ? 13 :
                x instanceof Date[] ? 14 : x instanceof Timestamp[] ? 15 : x instanceof Minute[] ? 17 : x instanceof Second[] ? 18 : x instanceof Time[] ? 19 :
                x instanceof Flip ? 98 : x instanceof Dict ? 99 : 0;
    }

    static int[] nt = {0, 1, 0, 0, 1, 2, 4, 8, 4, 8, 1, 0, 0, 4, 4, 8, 0, 4, 4, 4};

    static int ns(String s)
    {
        return s == null ? 0 : s.length();
    }

    static int n(Object x)
    {
        return x instanceof Dict ? n(((Dict) x).x) : x instanceof Flip ? n(((Flip) x).y[0]) : java.lang.reflect.Array.getLength(x);
    }

    static int nx(Object x)
    {
        int i = 0,n,t = t(x),j;
        if (t == 99) return 1 + nx(((Dict) x).x) + nx(((Dict) x).y);
        if (t == 98) return 3 + nx(((Flip) x).x) + nx(((Flip) x).y);
        if (t < 0) return t == -11 ? 2 + ns((String) x) : 1 + nt[-t];
        j = 6;
        n = n(x);
        if (t == 0 || t == 11) for (; i < n; ++i) j += t == 0 ? nx(((Object[]) x)[i]) : 1 + ns(((String[]) x)[i]); else j += n * nt[t];
        return j;
    }

    public static boolean qn(short x)
    {
        return x == Short.MIN_VALUE;
    }

    public static boolean qn(int x)
    {
        return x == Integer.MIN_VALUE;
    }

    public static boolean qn(long x)
    {
        return x == Long.MIN_VALUE;
    }

    public static boolean qn(float x)
    {
        return Float.isNaN(x);
    }

    public static boolean qn(double x)
    {
        return Double.isNaN(x);
    }

    public static Object at(Object x, int i)
    {
        int t = t(x);
        if (t == 0 || t > 10)
            x = ((Object[]) x)[i];
        else if (t == 1)
            x = new Boolean(((boolean[]) x)[i]);
        else if (t == 4)
            x = new Byte(((byte[]) x)[i]);
        else if (t == 5)
        {
            short a = ((short[]) x)[i];
            x = qn(a) ? null : new Short(a);
        }
        else if (t == 6)
        {
            int a = ((int[]) x)[i];
            x = qn(a) ? null : new Integer(a);
        }
        else if (t == 7)
        {
            long a = ((long[]) x)[i];
            x = qn(a) ? null : new Long(a);
        }
        else if (t == 8)
        {
            float a = ((float[]) x)[i];
            x = qn(a) ? null : new Float(a);
        }
        else if (t == 9)
        {
            double a = ((double[]) x)[i];
            x = qn(a) ? null : new Double(a);
        }
        else
            x = new Character(((char[]) x)[i]);
        return x;
    }

    void w(byte x)
    {
        B[J++] = x;
    }

    void w(boolean x)
    {
        w((byte) (x ? 1 : 0));
    }

    void w(short h)
    {
        w((byte) (h >> 8));
        w((byte) h);
    }

    void w(int i)
    {
        w((short) (i >> 16));
        w((short) i);
    }

    void w(long j)
    {
        w((int) (j >> 32));
        w((int) j);
    }

    void w(float e)
    {
        w(Float.floatToIntBits(e));
    }

    void w(double f)
    {
        w(Double.doubleToLongBits(f));
    }

    void w(char c)
    {
        w((byte) c);
    }

    void w(String s)
    {
        int i = 0,n = ns(s);
        for (; i < n;) w(s.charAt(i++));
        B[J++] = 0;
    }

    void w(Date d)
    {
        w((int) (d.getTime() / 86400000 - 10957));
    }

    void w(Timestamp z)
    {
        w(z.getTime() / 8.64e7 - 10957);
    }

    void w(Time t)
    {
        w((int) t.getTime());
    }

    void w(Month m)
    {
        w(m.i);
    }

    void w(Minute u)
    {
        w(u.i);
    }

    void w(Second v)
    {
        w(v.i);
    }

    void w(Object x)
    {
        int i = 0,n,t = t(x);
        w((byte) t);
        if (t < 0)
            switch (t)
            {
                case -1:
                    w(((Boolean) x).booleanValue());
                    return;
                case -4:
                    w(((Byte) x).byteValue());
                    return;
                case -5:
                    w(((Short) x).shortValue());
                    return;
                case -6:
                    w(((Integer) x).intValue());
                    return;
                case -7:
                    w(((Long) x).longValue());
                    return;
                case -8:
                    w(((Float) x).floatValue());
                    return;
                case -9:
                    w(((Double) x).doubleValue());
                    return;
                case -10:
                    w(((Character) x).charValue());
                    return;
                case -11:
                    w((String) x);
                    return;
                case -13:
                    w((Month) x);
                    return;
                case -14:
                    w((Date) x);
                    return;
                case -15:
                    w((Timestamp) x);
                    return;
                case -17:
                    w((Minute) x);
                    return;
                case -18:
                    w((Second) x);
                    return;
                case -19:
                    w((Time) x);
                    return;
            }
        if (t == 99)
        {
            Dict r = (Dict) x;
            w(r.x);
            w(r.y);
            return;
        }
        B[J++] = 0;
        if (t == 98)
        {
            Flip r = (Flip) x;
            B[J++] = 99;
            w(r.x);
            w(r.y);
            return;
        }
        w(n = n(x));
        for (; i < n; ++i)
            if (t == 0)
                w(((Object[]) x)[i]);
            else if (t == 1)
                w(((boolean[]) x)[i]);
            else if (t == 4)
                w(((byte[]) x)[i]);
            else if (t == 5)
                w(((short[]) x)[i]);
            else if (t == 6)
                w(((int[]) x)[i]);
            else if (t == 7)
                w(((long[]) x)[i]);
            else if (t == 8)
                w(((float[]) x)[i]);
            else if (t == 9)
                w(((double[]) x)[i]);
            else if (t == 10)
                w(((char[]) x)[i]);
            else if (t == 11)
                w(((String[]) x)[i]);
            else if (t == 13)
                w(((Month[]) x)[i]);
            else if (t == 14)
                w(((Date[]) x)[i]);
            else if (t == 15)
                w(((Timestamp[]) x)[i]);
            else if (t == 17)
                w(((Minute[]) x)[i]);
            else if (t == 18)
                w(((Second[]) x)[i]);
            else
                w(((Time[]) x)[i]);
    }

    boolean rb()
    {
        return 1 == b[j++];
    }

    short rh()
    {
        int x = b[j++],y = b[j++];
        return (short) (a ? x & 0xff | y << 8 : x << 8 | y & 0xff);
    }

    int ri()
    {
        int x = rh(),y = rh();
        return a ? x & 0xffff | y << 16 : x << 16 | y & 0xffff;
    }

    long rj()
    {
        int x = ri(),y = ri();
        return a ? x & 0xffffffffL | (long) y << 32 : (long) x << 32 | y & 0xffffffffL;
    }

    float re()
    {
        return Float.intBitsToFloat(ri());
    }

    double rf()
    {
        return Double.longBitsToDouble(rj());
    }

    char rc()
    {
        return (char) (b[j++] & 0xff);
    }

    String rs()
    {
        int k = j;
        for (; b[k] != 0;) ++k;
        char[] s = new char[k - j];
        for (int i = 0; j < k;) s[i++] = (char) (0xFF & b[j++]);
        ++j;
        return new String(s);
    }

    Minute ru()
    {
        int i = ri();
        return qn(i) ? null : new Minute(i);
    }

    Month rm()
    {
        int i = ri();
        return qn(i) ? null : new Month(i);
    }

    Second rv()
    {
        int i = ri();
        return qn(i) ? null : new Second(i);
    }

    Time rt()
    {
        int i = ri();
        return qn(i) ? null : new KTime(i);
    }

    Date rd()
    {
        int i = ri();
        return qn(i) ? null : new KDate(86400000L * (i + 10957));
    }

    Timestamp rz()
    {
        double f = rf();
        return qn(f) ? null : new KTimestamp((long) (.5 + 8.64e7 * (f + 10957)));
    }

    Object r()
    {
        int i = 0,n,t = b[j++];
        if (t < 0)
            switch (t)
            {
                case -1:
                    return new Boolean(rb());
                case -4:
                    return new Byte(b[j++]);
                case -5:
                    return new Short(rh());
                case -6:
                    return new Integer(ri());
                case -7:
                    return new Long(rj());
                case -8:
                    return new Float(re());
                case -9:
                    return new Double(rf());
                case -10:
                    return new Character(rc());
                case -11:
                    return rs();
                case -13:
                    return rm();
                case -14:
                    return rd();
                case -15:
                    return rz();
                case -17:
                    return ru();
                case -18:
                    return rv();
                case -19:
                    return rt();
            }
        if (t > 99)
        {
            j++;
            return null;
        }
        if (t == 99) return new Dict(r(), r());
        j++;
        if (t == 98) return new Flip((Dict) r());
        n = ri();
        switch (t)
        {
            case 0:
                Object[] L = new Object[n];
                for (; i < n; i++) L[i] = r();
                return L;
            case 1:
                boolean[] B = new boolean[n];
                for (; i < n; i++) B[i] = rb();
                return B;
            case 4:
                byte[] G = new byte[n];
                for (; i < n; i++) G[i] = b[j++];
                return G;
            case 5:
                short[] H = new short[n];
                for (; i < n; i++) H[i] = rh();
                return H;
            case 6:
                int[] I = new int[n];
                for (; i < n; i++) I[i] = ri();
                return I;
            case 7:
                long[] J = new long[n];
                for (; i < n; i++) J[i] = rj();
                return J;
            case 8:
                float[] E = new float[n];
                for (; i < n; i++) E[i] = re();
                return E;
            case 9:
                double[] F = new double[n];
                for (; i < n; i++) F[i] = rf();
                return F;
            case 10:
                char[] C = new char[n];
                for (; i < n; i++) C[i] = rc();
                return C;
            case 11:
                String[] S = new String[n];
                for (; i < n; i++) S[i] = rs();
                return S;
            case 13:
                Month[] M = new Month[n];
                for (; i < n; i++) M[i] = rm();
                return M;
            case 14:
                Date[] D = new Date[n];
                for (; i < n; i++) D[i] = rd();
                return D;
            case 17:
                Minute[] U = new Minute[n];
                for (; i < n; i++) U[i] = ru();
                return U;
            case 15:
                Timestamp[] Z = new Timestamp[n];
                for (; i < n; i++) Z[i] = rz();
                return Z;
            case 18:
                Second[] V = new Second[n];
                for (; i < n; i++) V[i] = rv();
                return V;
            case 19:
                Time[] T = new Time[n];
                for (; i < n; i++) T[i] = rt();
                return T;
        }
        return null;
    }

    void w(int i, Object x) throws IOException
    {
        int n = nx(x) + 8;
        B = new byte[n];
        B[0] = 0;
        B[1] = (byte) i;
        J = 4;
        w(n);
        w(x);
        o.write(B);
    }

    public void ks(String s) throws IOException
    {
        w(0, cs(s));
    }

    public void ks(Object o) throws IOException
    {
        w(0, o);
    }

    char[] cs(String s)
    {
        return s.toCharArray();
    }

    public void ks(String s, Object x) throws IOException
    {
        Object[] a = {cs(s), x};
        w(0, a);
    }

    public void ks(String s, Object x, Object y) throws IOException
    {
        Object[] a = {cs(s), x, y};
        w(0, a);
    }

    public void ks(String s, Object x, Object y, Object z) throws IOException
    {
        Object[] a = {cs(s), x, y, z};
        w(0, a);
    }

    public static class K4Exception extends Exception
    {
        K4Exception(String s)
        {
            super(s);
        }
    }

    public synchronized Object k() throws K4Exception, IOException
    {
        i.readFully(b = new byte[8]);
        a = b[0] == 1;
        j = 4;
        i.readFully(b = new byte[ri() - 8]);
        if (b[0] == -128)
        {
            j = 1;
            throw new K4Exception(rs());
        }
        j = 0;
        return r();
    }

    public Object k(Object x) throws K4Exception, IOException
    {
        w(1, x);
        return k();
    }

    public Object k(String s) throws K4Exception, IOException
    {
        return k(cs(s));
    }

    public Object k(String s, Object x) throws K4Exception, IOException
    {
        Object[] a = {cs(s), x};
        return k(a);
    }

    public Object k(String s, Object x, Object y) throws K4Exception, IOException
    {
        Object[] a = {cs(s), x, y};
        return k(a);
    }

    public Object k(String s, Object x, Object y, Object z) throws K4Exception, IOException
    {
        Object[] a = {cs(s), x, y, z};
        return k(a);
    }
}

/*
static Class[]c={int[].class,boolean[].class,null,null,byte[].class,short[].class,int[].class};
static int foo(Class x){if(x.isArray())for(int i=0;i<9;++i)if(c[i]==x)return i;return 0;}
boolean b;int[]q=new int[2];tm();for(int i=0;i<1000000;++i)foo(q.getClass());tm();
static java.util.TimeZone tz=java.util.TimeZone.getDefault();static int tzo(long x){return tz.getOffset(x);}
static long lg(long x){x+=tzo(x);return x<0?x+86400000:x;}static long gl(long x){long t=x-tzo(x);return t+x-lg(t);}
Time(gl(i));}Date rd(){int i=ri();return qn(i)?null:new Date(gl(86400000L*(i+10957)));}
5000 roundtrip
Object: 4+16Integer/Double|24Date/String + strings
c c=new c((new ServerSocket(2000)).accept());for(;;)c.w(2,c.k());
*/