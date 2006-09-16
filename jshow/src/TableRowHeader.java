/***********************************************************************************************************************
 *
 * Author:    Charlie Skelton, Skelton Consulting GmbH
 * Copyright: Skelton Consulting GmbH, Stuttgart, Germany
 * License:   Creative Commons, Attribution-ShareAlike 2.0
 *            see http://creativecommons.org/licenses/by-sa/2.0/legalcode
 *            or the included license.txt file for full license details
 */

import javax.swing.*;
import java.awt.*;

public class TableRowHeader extends JList
{
    private JTable table;

    public TableRowHeader(JTable table)
    {
        this.table = table;
        setAutoscrolls(false);
        setCellRenderer(new RowHeaderRenderer());
        setFixedCellHeight(table.getRowHeight());
        setFixedCellWidth(50);
        setFocusable(false);
        setModel(new TableListModel());
        setOpaque(false);
        setSelectionModel(table.getSelectionModel());
    }

    class TableListModel extends AbstractListModel
    {
        public int getSize()
        {
            return table.getRowCount();
        }

        public Object getElementAt(int index)
        {
            return String.valueOf(index + 1);
        }
    }

    class RowHeaderRenderer extends JLabel implements ListCellRenderer
    {
        RowHeaderRenderer()
        {
            setHorizontalAlignment(CENTER);
            setOpaque(true);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setFont(table.getTableHeader().getFont());
            setBackground(table.getTableHeader().getBackground());
            setForeground(table.getTableHeader().getForeground());
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            setBackground(table.getTableHeader().getBackground());

            setText((value == null) ? "" : value.toString());

            return this;
        }
    }
}