/***********************************************************************************************************************
 *
 * Author:    Charlie Skelton, Skelton Consulting GmbH
 * Copyright: Skelton Consulting GmbH, Stuttgart, Germany
 * License:   Creative Commons, Attribution-ShareAlike 2.0
 *            see http://creativecommons.org/licenses/by-sa/2.0/legalcode
 *            or the included license.txt file for full license details
 */

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class AutoFitTableColumns
{
    private static final int DEFAULT_COLUMN_PADDING = 5;

    public static int autoResizeTable( JTable  table,
                                       boolean includeColumnHeaderWidth)
    {
        return( autoResizeTable( table, includeColumnHeaderWidth, DEFAULT_COLUMN_PADDING));
    }

    public static int autoResizeTable(JTable  table,
                                      boolean includeColumnHeaderWidth,
                                      int     columnPadding)
    {
        int tableWidth = 0;

        Dimension cellSpacing = table.getIntercellSpacing();

        int columnCount = table.getColumnCount();

        if (columnCount > 0)
        {
            int columnWidth[] = new int[columnCount];

            for (int i = 0; i < columnCount; i++)
            {
                columnWidth[i] = getMaxColumnWidth(table, i, true, columnPadding);
                columnWidth[i]+=5; // cgs
                tableWidth += columnWidth[i];
            }

            tableWidth += ((columnCount - 1) * cellSpacing.width);

            JTableHeader tableHeader = table.getTableHeader();
            Dimension headerDim = tableHeader.getPreferredSize();
            headerDim.width = tableWidth;
            tableHeader.setPreferredSize(headerDim);

            Dimension interCellSpacing = table.getIntercellSpacing();
            Dimension dim = new Dimension();
            int rowHeight = table.getRowHeight();
            if (rowHeight == 0)
                rowHeight = 16;

            dim.height = headerDim.height + ((rowHeight + interCellSpacing.height) * table.getRowCount());
            dim.width = tableWidth;
            TableColumnModel tableColumnModel= table.getColumnModel();

            for (int i = 0; i < columnCount; i++)
            {
                TableColumn tableColumn = tableColumnModel.getColumn(i);
                tableColumn.setPreferredWidth(columnWidth[i]);
            }

            table.invalidate();
            table.doLayout();
            table.repaint();
        }

        return (tableWidth);
    }


    private static int getMaxColumnWidth( JTable  table,
                                          int     columnNo,
                                          boolean includeColumnHeaderWidth,
                                          int     columnPadding)
    {
        TableColumn column = table.getColumnModel().getColumn(columnNo);
        Component comp = null;
        int maxWidth = 0;
        if (includeColumnHeaderWidth)
        {
            TableCellRenderer headerRenderer = column.getHeaderRenderer();
            if (headerRenderer != null)
            {
                comp = headerRenderer.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, 0, columnNo);
                if (comp instanceof JTextComponent)
                {
                    JTextComponent jtextComp = (JTextComponent) comp;
                    String text = jtextComp.getText();
                    Font font = jtextComp.getFont();
                    FontMetrics fontMetrics = jtextComp.getFontMetrics(font);
                    maxWidth = SwingUtilities.computeStringWidth(fontMetrics, text);
                }
                else
                    maxWidth = comp.getPreferredSize().width;
            }
            else
            {
                try
                {
                    String headerText = (String) column.getHeaderValue();
                    JLabel defaultLabel = new JLabel(headerText);
                    Font font = defaultLabel.getFont();
                    FontMetrics fontMetrics = defaultLabel.getFontMetrics(font);
                    maxWidth = SwingUtilities.computeStringWidth(fontMetrics, headerText);
                }
                catch (ClassCastException ce)
                {
                    maxWidth = 0;
                }
            }

            maxWidth+= 20; // allow for arrows on sorter
        }


        int cellWidth = 0;
        FontMetrics fontMetrics= null;

        int maxRow= table.getRowCount();

        int stepSize= maxRow/1000;

        if( stepSize == 0)
        {
            stepSize=1;
        }

        TableCellRenderer tableCellRenderer= null;

        for( int row= 0; row < maxRow; row += stepSize)
        {
            if( tableCellRenderer == null)
            {
                tableCellRenderer = table.getCellRenderer(row, columnNo);
            }

            comp= tableCellRenderer.getTableCellRendererComponent( table,
                                                                   table.getValueAt(row, columnNo),
                                                                   false,
                                                                   false,
                                                                   row,
                                                                   columnNo);
            if (comp instanceof JTextComponent)
            {
                JTextComponent jtextComp = (JTextComponent) comp;

                if( fontMetrics == null)
                {
                    Font font = jtextComp.getFont();
                    fontMetrics = jtextComp.getFontMetrics(font);
                }

                String text = jtextComp.getText();

                int textWidth = SwingUtilities.computeStringWidth(fontMetrics, text);
                maxWidth = Math.max(maxWidth, textWidth);
            }
            else
            {
                cellWidth = comp.getPreferredSize().width;
                maxWidth = Math.max(maxWidth, cellWidth);
            }
        }

        return (maxWidth + columnPadding);
    }
}

