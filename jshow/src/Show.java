/***********************************************************************************************************************
 *
 * Author:    Charlie Skelton, Skelton Consulting GmbH
 * Copyright: Skelton Consulting GmbH, Stuttgart, Germany
 * License:   Creative Commons, Attribution-ShareAlike 2.0
 *            see http://creativecommons.org/licenses/by-sa/2.0/legalcode
 *            or the included license.txt file for full license details
 */

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;


public class Show extends JPanel
{
    private static String filename = null;
    private static final String version = "jshow 1.21";

    private TableModel model;

    public int getRowCount()
    {
        return model.getRowCount();
    }

    public Show(final JFrame frame, final Object obj)
    {
        super(new GridLayout(1, 0));

        model = new K4TableModel(obj);
        SortableTableModel sorter = null;

        if (model.getRowCount() < 50000)
        {
            sorter = new SortableTableModel(model);
            model = sorter;
        }

        JTable table = new JTable()
        {
            Color col = new Color(0xee, 0xee, 0xff);
            Color csel = new Color(0xaa, 0xaa, 0xff);
            Color keyColor = new Color(0xdd, 0xff, 0xdd);

            public Component prepareRenderer(TableCellRenderer renderer,
                                             int rowIndex,
                                             int vColIndex)
            {
                Color bg = null;

                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);

                if (isCellSelected(rowIndex, vColIndex))
                {
                    bg = csel;
                }
                else
                {
                    TableModel model = getModel();
                    if (model instanceof SortableTableModel)
                    {
                        model = ((SortableTableModel) model).getTableModel();
                    }

                    if (model instanceof KTableModel)
                    {
                        boolean key = ((KTableModel) model).isKey(vColIndex);

                        if (key)
                        {
                            bg = keyColor;
                        }
                    }
                }

                if (bg == null)
                {
                    if (rowIndex % 2 == 0)
                    {
                        bg = col;
                    }
                    else
                    {
                        bg = getBackground();
                    }
                }

                c.setBackground(bg);

                return c;
            }
        };

        table.setModel(model);

        DefaultTableCellRenderer dhr = new TableHeaderRenderer(table);
        table.getTableHeader().setDefaultRenderer(dhr);

        if (sorter != null)
        {
            sorter.setTableHeader(table.getTableHeader());
        }

        table.setDragEnabled(true);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        table.setCellSelectionEnabled(true);

        ToolTipManager.sharedInstance().unregisterComponent(table);
        ToolTipManager.sharedInstance().unregisterComponent(table.getTableHeader());

        DefaultTableCellRenderer dcr = new CellRenderer(table);
        dcr.setForeground(Color.BLACK);
        dcr.setBackground(Color.WHITE);
        dcr.setHorizontalAlignment(SwingConstants.RIGHT);

        for (int i = 0; i < model.getColumnCount(); i++)
        {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setCellRenderer(dcr);
        }

        AutoFitTableColumns.autoResizeTable(table, true);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getTableHeader().setReorderingAllowed(false);

        int width = 0;
        for (int i = 0; i < model.getColumnCount(); i++)
        {
            TableColumn col = table.getColumnModel().getColumn(i);
            width += col.getPreferredWidth();
        }

        int maxWidth = (int) (java.awt.Toolkit.getDefaultToolkit().getScreenSize().width * 0.9);

        width = width > maxWidth ? maxWidth : width;

        int maxHeight = (int) (java.awt.Toolkit.getDefaultToolkit().getScreenSize().height * 0.6);

        int height = table.getRowCount() * table.getRowHeight();

        height = height > maxHeight ? maxHeight : height;

        table.setPreferredScrollableViewportSize(new Dimension(width, height));

        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.setRowHeaderView(new TableRowHeader(table));

        add(scrollPane);

        JMenuBar menubar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);

        final JTable _table = table;
 
        UserAction exportAction = new UserAction("Export ...",
                                                 Utils.getImage("/de/skelton/images/export1.png"),
                                                 "export data to csv",
                                                 new Integer(KeyEvent.VK_A),
                                                 null)
        {
            public void actionPerformed(ActionEvent ev)
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogType(JFileChooser.SAVE_DIALOG);
                chooser.setDialogTitle("Export result set as");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                chooser.addChoosableFileFilter(
                        new FileFilter()
                        {
                            public String getDescription()
                            {
                                return "csv (Comma delimited)";
                            }

                            public boolean accept(File file)
                            {
                                if (file.isDirectory() || file.getName().endsWith(".csv"))
                                {
                                    return true;
                                }
                                else
                                {
                                    return false;
                                }
                            }
                        }
                );

                if (filename != null)
                {
                    File file = new File(filename);
                    File dir = new File(file.getPath());
                    chooser.setCurrentDirectory(dir);
                }

                int option = chooser.showSaveDialog(frame);

                if (option == JFileChooser.APPROVE_OPTION)
                {
                    File sf = chooser.getSelectedFile();
                    File f = chooser.getCurrentDirectory();
                    String dir = f.getAbsolutePath();

                    try
                    {
                        filename = dir + "/" + sf.getName();

                        Runnable runner = new Runnable()
                        {
                            public void run()
                            {
                                TableExporter.exportTable(_table, filename);
                            }
                        };
                        Thread t = new Thread(runner);
                        t.start();
                    }
                    catch (Exception e)
                    {
                    }
                }

            }
        };

        UserAction closeAction = new UserAction("Close",
                                                Utils.getImage("/de/skelton/images/document_delete.png"),
                                                "Close this window",
                                                new Integer(KeyEvent.VK_X),
                                                null)
        {
            public void actionPerformed(ActionEvent e)
            {
                closeWindow(frame);
            }
        };

        UserAction chartAction = new UserAction("Chart",
                                                 Utils.getImage("/de/skelton/images/chart.png"),
                                                 "Chart the table",
                                                 new Integer(KeyEvent.VK_A),
                                                 null)
        {
            public void actionPerformed(ActionEvent e)
            {
                new LineChart(_table);
            }
        };

        if( table.getRowCount() < 200000)
        {
            menu.add(new JMenuItem(chartAction));
            menu.addSeparator();
        }

        menu.add(new JMenuItem(exportAction));
        menu.addSeparator();
        menu.add(new JMenuItem(closeAction));
        menubar.add(menu);

        frame.setJMenuBar(menubar);
    }

    private static Map frameMap= Collections.synchronizedMap( new HashMap());
    private static int windowId= 0;

    public static void closeWindows(c c)
    {
        List list= new LinkedList();
        Set keys= frameMap.keySet();
        Iterator i= keys.iterator();
        String prefix= c.toString();

        while(i.hasNext())
        {
            Object key= i.next();

            if( (key instanceof String) && ((String)key).startsWith( prefix))
            {
                 list.add(frameMap.get(key));
            }
        }

        i= list.iterator();
        while(i.hasNext())
            closeWindow((JFrame) i.next());
    }

    public static void closeWindow( JFrame frame)
    {
        Set keys= frameMap.keySet();
        Iterator i= keys.iterator();

        while(i.hasNext())
        {
            Object key= i.next();

            JFrame f= (JFrame) frameMap.get(key);
            if( frame == f)
            {
                frameMap.remove( key);
                break;
            }

        }

        frame.setVisible( false );
        frame.dispose();
    }

    private static JFrame createAndShowGUI(c c, String title, Object obj)
    {
        boolean frameReused= false;

        JFrame.setDefaultLookAndFeelDecorated(true);

        JFrame frame= null;

        if( title != null)
            frame= (JFrame) frameMap.get( c.toString()+title);

        if( frame == null)
            frame= new JFrame();
        else
            frameReused= true;

        if( title != null)
            frameMap.put( c.toString()+title, frame);
        else
            frameMap.put( c.toString()+windowId++, frame);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final JFrame _frame= frame;

        WindowListener windowListener = new WindowAdapter()
        {
           public void windowClosing ( WindowEvent w )
           {
               closeWindow(_frame);
           }
        };

        frame.addWindowListener( windowListener );

        final Show show = new Show(frame, obj);
        frame.setIconImage(Toolkit.getDefaultToolkit().createImage(show.getClass().getResource("de/skelton/images/table.png")));

        frame.setTitle( (title != null?title:version) + " :: " + show.getRowCount() + " rows");

        show.setOpaque(true);
        frame.setContentPane(show);

        if( ! frameReused)
            frame.pack();

        frame.setVisible(true);
        frame.requestFocus();
        frame.toFront();

        return frame;
    }

    public static void main(String[] args)
    {
        System.out.println(version);

        int port = 9999;

        if (args.length > 0)
        {
            try
            {
                port = Integer.parseInt(args[0]);
            }
            catch (Exception e)
            {
                System.err.println("Error: could not parse port number argument. Exiting.");
                System.exit(1);
            }
        }
        final int _port = port;

        Runnable runner = new Runnable()
        {
            public void run()
            {
                try
                {
                    final ServerSocket ss = new ServerSocket(_port);

                    System.out.println("Listening on port " + _port + " for connections...");

                    while (true)
                    {
                        try
                        {
                            final Socket s = ss.accept();

                            System.out.println("Connection from " + s.getRemoteSocketAddress().toString());

                            Runnable runner = new Runnable()
                            {
                                public void run()
                                {
                                    c c = null;

                                    try
                                    {
                                        c = new c(s);

                                        while (true)
                                        {
                                            String title= null;

                                            Object obj = c.k();

                                            if( obj != null)
                                            {
                                                if( (obj instanceof String) && "cls".equals((String)obj))
                                                {
                                                    closeWindows(c);
                                                }
                                                else if((obj instanceof Object[])
                                                    && (((Object[]) obj).length > 1)
                                                    && (((Object[]) obj)[0] instanceof String ))
                                                    {
                                                        title= (String) ((Object[]) obj)[0];
                                                        obj= ((Object[]) obj)[1];
                                                    }

                                                    final Object fobj= obj;
                                                    final String ftitle= title;
                                                    final c fc= c;

                                                    if( K4TableModel.isTable(obj))
                                                    {
                                                        System.out.println("Message received from " + s.getRemoteSocketAddress().toString());

                                                        javax.swing.SwingUtilities.invokeLater(new Runnable()
                                                        {
                                                            public void run()
                                                            {
                                                                try
                                                                {
                                                                    createAndShowGUI(fc, ftitle,fobj);
                                                                }
                                                                catch (Exception e)
                                                                {
                                                                }
                                                            }
                                                        });
                                                    }
                                            }
                                        }
                                    }
                                    catch(Exception e)
                                    {
                                        System.out.println("Connection closed from " + s.getRemoteSocketAddress().toString());
                                        //    e.printStackTrace();
                                    }
                                    finally
                                    {
                                        closeWindows(c);

                                        if( c != null)
                                        {
                                            c.close();
                                            c= null;
                                        }
                                    }
                                }
                            };

                            Thread t = new Thread(runner);
                            t.start();
                        }
                        catch (Exception e)
                        {
                        }
                    }
                }
                catch (Exception e)
                {
                    System.out.println("Error : " + e.getMessage());
                }
            }
        };
        Thread t = new Thread(runner);
        t.start();
    }
}
