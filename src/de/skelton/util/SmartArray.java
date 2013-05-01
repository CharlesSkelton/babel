/* Babel for kdb+ by Charles Skelton
   is licensed under a Creative Commons Attribution-Noncommercial-Share Alike 3.0 Germany License
   http://creativecommons.org/licenses/by-nc-sa/3.0
*/

package de.skelton.util;

import java.lang.reflect.Array;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;

public class SmartArray{
  int n=0;
  private Object a;
  public SmartArray(Class primitiveClass){this(8192,primitiveClass);}
  public SmartArray(int initialSize,Class primitiveClass){a=Array.newInstance(primitiveClass,initialSize);}
  private void checkCapacity(){
    if(n>=Array.getLength(a)){
      Object b=Array.newInstance(a.getClass().getComponentType(),2*Array.getLength(a));
      System.arraycopy(a,0,b,0,Array.getLength(a));
      a=b;b=null;
    }
  }
  public void add(byte[]b){checkCapacity();Array.set(a,n++,b);}
  public void add(char[]c){checkCapacity();Array.set(a,n++,c);}
  public void add(Time t){checkCapacity();Array.set(a,n++,t);}
  public void add(Timestamp t){checkCapacity();Array.set(a,n++,t);}
  public void add(Date d){checkCapacity();Array.set(a,n++,d);}
  public void add(long l){checkCapacity();Array.setLong(a,n++,l);}
  public void add(boolean b){checkCapacity();Array.setBoolean(a,n++,b);}
  public void add(float f){checkCapacity();Array.setFloat(a,n++,f);}
  public void add(double d){checkCapacity();Array.setDouble(a,n++,d);}
  public void add(int i){checkCapacity();Array.setInt(a,n++,i);}
//    public void append(short s){checkCapacity();Array.setShort(a,n++,s);}
//    public void append(String s){checkCapacity();Array.set(a,n++,s);}
  public Object compact(){Object b=Array.newInstance(a.getClass().getComponentType(),n);System.arraycopy(a,0,b,0,n);a=b;return a;}
}