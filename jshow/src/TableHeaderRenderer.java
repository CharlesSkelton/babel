/***********************************************************************************************************************
 *
 * Author:    Charlie Skelton, Skelton Consulting GmbH
 * Copyright: Skelton Consulting GmbH, Stuttgart, Germany
 * License:   Creative Commons, Attribution-ShareAlike 2.0
 *            see http://creativecommons.org/licenses/by-sa/2.0/legalcode
 *            or the included license.txt file for full license details
 */

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

class TableHeaderRenderer extends DefaultTableCellRenderer
{
    JLabel label = null;

    public TableHeaderRenderer(JTable table)
    {
        super();

        label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setOpaque(true);
        label.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        label.setFont(table.getTableHeader().getFont());
        label.setBackground(table.getTableHeader().getBackground());
        label.setForeground(table.getTableHeader().getForeground());
    }

    public Component getTableCellRendererComponent( JTable  table,
                                                    Object  value,
                                                    boolean isSelected,
                                                    boolean hasFocus,
                                                    int     row,
                                                    int     column)
    {
        if (value != null)
            label.setText(value.toString());
        else
            label.setText("");

        return label;
    }

    public void setToolTip(String toolTip)
    {
        if (toolTip != null)
            label.setToolTipText(toolTip);
    }

    public void setIcon(Icon icon)
    {
        if (label != null)
            label.setIcon(icon);
    }
}
