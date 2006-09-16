/***********************************************************************************************************************
 *
 * Author:    Charlie Skelton, Skelton Consulting GmbH
 * Copyright: Skelton Consulting GmbH, Stuttgart, Germany
 * License:   Creative Commons, Attribution-ShareAlike 2.0
 *            see http://creativecommons.org/licenses/by-sa/2.0/legalcode
 *            or the included license.txt file for full license details
 */

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.awt.*;

public class Utils
{
    public static String getExceptionTrace (Throwable e)
    {
       StringWriter strWriter = new StringWriter ();
       PrintWriter prtWriter = new PrintWriter (strWriter);
       e.printStackTrace (prtWriter);
       return (strWriter.toString ());
    }

    public static ImageIcon getImage(String strFilename)
    {
        Class thisClass = Utils.class;

        java.net.URL url = null;

        if (strFilename.startsWith("/"))
            url = thisClass.getResource(strFilename);
        else
            url = thisClass.getResource("/toolbarButtonGraphics/" + strFilename);

        if( url == null)
            return null;

        Image image = Toolkit.getDefaultToolkit().getImage(url);

        return new ImageIcon(image);
    }
}
