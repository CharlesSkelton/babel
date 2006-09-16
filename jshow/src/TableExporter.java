/***********************************************************************************************************************
 *
 * Author:    Charlie Skelton, Skelton Consulting GmbH
 * Copyright: Skelton Consulting GmbH, Stuttgart, Germany
 * License:   Creative Commons, Attribution-ShareAlike 2.0
 *            see http://creativecommons.org/licenses/by-sa/2.0/legalcode
 *            or the included license.txt file for full license details
 */

import javax.swing.*;
import java.io.*;

public class TableExporter
{
    public static void exportTable(JTable table, String filename)
    {
        if (filename != null)
        {
            String lineSeparator= (String) java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("line.separator"));

            PrintWriter pw= null;
            try
            {
                pw= new PrintWriter(new BufferedWriter(new FileWriter(filename)));

                for (int col = 0; col < table.getModel().getColumnCount(); col++)
                {
                    if (col > 0)
                        pw.write(",");

                    pw.write(table.getModel().getColumnName(col));
                }
                pw.write(lineSeparator);

                for (int r = 1; r <= table.getModel().getRowCount(); r++)
                {
                    for (int col = 0; col < table.getModel().getColumnCount(); col++)
                    {
                        if (col > 0)
                            pw.write(",");

                        pw.write(table.getModel().getValueAt(r - 1, col).toString());
                    }
                    pw.write(lineSeparator);
                }

                pw.close();
            }
            catch (FileNotFoundException ex)
            {
                ex.printStackTrace();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }
}
