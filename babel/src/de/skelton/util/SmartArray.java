/* Babel for kdb+ by Charles Skelton
   is licensed under a Creative Commons Attribution-Noncommercial-Share Alike 3.0 Germany License
   http://creativecommons.org/licenses/by-nc-sa/3.0
*/

package de.skelton.util;

import java.lang.reflect.Array;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class SmartArray
{
    int writePosition=0;
    private Object array;
    private int growthSize;

    public SmartArray(Class primitiveClass)
    {
        this(1024,primitiveClass);
    }

    public SmartArray(int initialSize,Class primitiveClass)
    {
        this(initialSize,(int)(initialSize/4),primitiveClass);
    }

    public SmartArray(int initialSize,int growthSize,Class primitiveClass)
    {
        this.growthSize=growthSize;
        array=Array.newInstance(primitiveClass,initialSize);
    }

    private void checkCapacity()
    {
        if(writePosition>=Array.getLength(array))
        {
            Object tmpArray=Array.newInstance(array.getClass().getComponentType(),Array.getLength(array)+growthSize);
            System.arraycopy(array,0,tmpArray,0,Array.getLength(array));
            array=tmpArray;
        }
    }

    public void append(Time t)
    {
        Array.set(array,writePosition++,t);
    }

    public void append(Timestamp t)
    {
        Array.set(array,writePosition++,t);
    }

    public void append(Date d)
    {
        Array.set(array,writePosition++,d);
    }

    public void append(short s)
    {
        Array.setShort(array,writePosition++,s);
    }

    public void append(long l)
    {
        Array.setLong(array,writePosition++,l);
    }

    public void append(boolean b)
    {
        Array.setBoolean(array,writePosition++,b);
    }

    public void append(float f)
    {
        Array.setFloat(array,writePosition++,f);
    }

    public void append(double d)
    {
        Array.setDouble(array,writePosition++,d);
    }

    public void append(int i)
    {
        Array.setInt(array,writePosition++,i);
    }

    public void append(String s)
    {
        Array.set(array,writePosition++,s);
    }

    public Object toArray()
    {
        Object trimmedArray=Array.newInstance(array.getClass().getComponentType(),writePosition);
        System.arraycopy(array,0,trimmedArray,0,writePosition);
        return trimmedArray;
    }
}