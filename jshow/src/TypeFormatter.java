/***********************************************************************************************************************
 *
 * Author:    Charlie Skelton, Skelton Consulting GmbH
 * Copyright: Skelton Consulting GmbH, Stuttgart, Germany
 * License:   Creative Commons, Attribution-ShareAlike 2.0
 *            see http://creativecommons.org/licenses/by-sa/2.0/legalcode
 *            or the included license.txt file for full license details
 */
import java.util.TimeZone;
import java.sql.Time;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import kx.c;

public class TypeFormatter
{
    private static SimpleDateFormat timeFormatter  = new SimpleDateFormat( "HH.mm.ss.SSS");
    private static SimpleDateFormat dateFormatter  = new SimpleDateFormat( "yyyy.MM.dd");
    private static NumberFormat     numberFormatter= new DecimalFormat( "#.#######");

    static
    {
        timeFormatter.setTimeZone( TimeZone.getTimeZone("GMT"));
        dateFormatter.setTimeZone( TimeZone.getTimeZone("GMT"));
    }

    public synchronized static String format( Object value)
    {
        String text= null;

        if( value == null)
        {
            return "NULL";
        }
        else if( value instanceof Time)
        {
            text= c.sd( (Time)value);
        }
        else if( value instanceof Date)
        {
            text= c.sd( (Date)value);
        }
        else if( value instanceof Timestamp)
        {
            text= c.sd( (Timestamp)value);
        }
        else if(value instanceof Double)
        {
            text= numberFormatter.format((Double)value);
        }
        else if( value instanceof String)
        {
            text= (String) value;

            if( text.length() == 0)
                text= null;
        }
        else if( value.getClass().isArray())
        {
            text= "...";
        }
        else {
            text= value.toString();
        }

        return text;
    }
}
