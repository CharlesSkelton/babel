/***********************************************************************************************************************
 *
 * Author:    Charlie Skelton, Skelton Consulting GmbH
 * Copyright: Skelton Consulting GmbH, Stuttgart, Germany
 * License:   Creative Commons, Attribution-ShareAlike 2.0
 *            see http://creativecommons.org/licenses/by-sa/2.0/legalcode
 *            or the included license.txt file for full license details
 */
import java.io.UnsupportedEncodingException;
import javax.swing.table.AbstractTableModel;
import java.lang.reflect.Array;
import kx.c;

public class K4TableModel extends AbstractTableModel implements KTableModel
{
    private boolean [] keys=     new boolean[0];
    private Object  [] columns=  new Object[0];
    private String  [] headers=  new String [0];
    private int        rowCount= 0;

    public void setData( Object obj) throws UnsupportedEncodingException
    {
        if( obj instanceof c.Dict)
        {
            // We get a dict when we have a key on a table. It is a dict with a flip as the key
            // and a flip as the value
            c.Dict d= (c.Dict)obj;

            if( (d.x instanceof c.Flip) && (d.y instanceof c.Flip))
            {
                int nkeys= ((c.Flip)d.x).x.length;

                int nCols= nkeys +((c.Flip)d.y).x.length;
                keys= new boolean[nCols];

                for( int i=0;i<nkeys;i++)
                {
                    keys[i]=true;
                }

                obj= c.td( obj);
            }
        }

        if( obj instanceof c.Flip)
        {
            c.Flip f= (c.Flip) obj;

            headers= f.x;

            if( keys.length != headers.length)
            {
                keys= new boolean[headers.length];
            }

            columns= f.y;

            if( columns.length > 0)
            {
                if( columns[0].getClass().isArray())
                {
                    rowCount= Array.getLength( columns[0]);
                }
            }
        }
    }

    public static boolean isTable( Object obj)
    {
        if( obj instanceof c.Flip)
        {
            return true;
        }
        else if( obj instanceof c.Dict)
        {
            c.Dict d= (c.Dict)obj;

            if( (d.x instanceof c.Flip) && (d.y instanceof c.Flip))
            {
                return true;
            }
        }

        return false;
    }

    public K4TableModel()
    {
    }

    public K4TableModel( Object obj) throws UnsupportedEncodingException
    {
        setData( obj);
    }

    public boolean isKey( int column)
    {
        return keys[column];
    }

    public int getColumnCount()
    {
        return columns.length;
    }

    public int getRowCount()
    {
        return rowCount;
    }

    public Object getValueAt(int row, int col)
    {
          return c.at(columns[ col],row);
    }


    public String getColumnName( int i)
    {
        return headers[ i];
    }

    public Class getColumnClass(int col)
    {
        Object o= columns[ col];
        return o.getClass().getComponentType();
    }
};

