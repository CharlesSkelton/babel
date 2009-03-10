package de.skelton.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.*;
import java.util.*;

public abstract class Logger {
   private static boolean on = true;

   private static SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy.dd.MM HH:mm:ss.SSS");

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