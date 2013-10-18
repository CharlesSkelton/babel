/* Babel for kdb+ by Charles Skelton
   is licensed under a Creative Commons Attribution-Noncommercial-Share Alike 3.0 Germany License
   http://creativecommons.org/licenses/by-nc-sa/3.0
*/
	  
package de.skelton.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.*;
import java.util.*;

public abstract class Logger {
   private static boolean on = true;

   private static SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");

   public static boolean isOn() {
      return on;
   }

   public static void setOn(boolean isOn) {
      on = isOn;
   }

   public static synchronized void log(String msg) {
      if (on) {
          System.out.println(dateFormat.format(new Date()) + " " + msg);
      }
   }
   
   public static void log(Throwable e)
    {
       StringWriter strWriter = new StringWriter ();
       PrintWriter prtWriter = new PrintWriter (strWriter);
       e.printStackTrace(prtWriter);
       log(strWriter.toString());
    }

}