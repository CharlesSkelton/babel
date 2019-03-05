/* Babel for kdb+
   Copyright 2019 Charles Skelton

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
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
