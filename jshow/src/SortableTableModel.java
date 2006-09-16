/***********************************************************************************************************************
 *
 * Author:    Charlie Skelton, Skelton Consulting GmbH
 * Copyright: Skelton Consulting GmbH, Stuttgart, Germany
 * License:   Creative Commons, Attribution-ShareAlike 2.0
 *            see http://creativecommons.org/licenses/by-sa/2.0/legalcode
 *            or the included license.txt file for full license details
 */

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;


public class SortableTableModel extends AbstractTableModel
{
    protected TableModel tableModel;

    public static final int DESCENDING = -1;
    public static final int NOT_SORTED = 0;
    public static final int ASCENDING  = 1;

    private static Directive EMPTY_DIRECTIVE = new Directive(-1, NOT_SORTED);

    public static final Comparator COMPARABLE_COMAPRATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            return ((Comparable) o1).compareTo(o2);
        }
    };

    public static final Comparator LEXICAL_COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            return o1.toString().compareTo(o2.toString());
        }
    };

    public static final Comparator DOUBLE_COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            int res = ((Double) o1).compareTo((Double) o2);
            return res;
        }
    };


    public static final Comparator TIME_COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            return ((Time) o1).compareTo((Time) o2);
        }
    };

    public static final Comparator TIMESTAMP_COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            return ((Timestamp) o1).compareTo((Timestamp) o2);
        }
    };

    public static final Comparator STRING_COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            return ((String) o1).compareTo((String) o2);
        }
    };

    public static final Comparator FLOAT_COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            return ((Float) o1).compareTo((Float) o2);
        }
    };

    public static final Comparator DATE_COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            return ((Date) o1).compareTo((Date) o2);
        }
    };

    public static final Comparator INTEGER_COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            return ((Integer) o1).compareTo((Integer) o2);
        }
    };

    public static final Comparator LONG_COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            return ((Long) o1).compareTo((Long) o2);
        }
    };

    public static final Comparator SHORT_COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            return ((Short) o1).compareTo((Short) o2);
        }
    };


    private Row[] viewToModel;
    private int[] modelToView;

    private JTableHeader tableHeader;
    private MouseListener mouseListener;
    private TableModelListener tableModelListener;
    private Map columnComparators = new HashMap();
    private List sortingColumns = new ArrayList();

    public SortableTableModel()
    {
        this.mouseListener = new MouseHandler();
        this.tableModelListener = new TableModelHandler();

        setColumnComparator(double.class,    DOUBLE_COMPARATOR);
        setColumnComparator(int.class,       INTEGER_COMPARATOR);
        setColumnComparator(float.class,     FLOAT_COMPARATOR);
        setColumnComparator(short.class,     SHORT_COMPARATOR);
        setColumnComparator(long.class,      LONG_COMPARATOR);

        setColumnComparator(Double.class,    DOUBLE_COMPARATOR);
        setColumnComparator(Integer.class,   INTEGER_COMPARATOR);
        setColumnComparator(Float.class,     FLOAT_COMPARATOR);
        setColumnComparator(Short.class,     SHORT_COMPARATOR);
        setColumnComparator(Long.class,      LONG_COMPARATOR);
        setColumnComparator(String.class,    STRING_COMPARATOR);
        setColumnComparator(Date.class,      DATE_COMPARATOR);
        setColumnComparator(Time.class,      TIME_COMPARATOR);
        setColumnComparator(Timestamp.class, TIMESTAMP_COMPARATOR);
    }

    public SortableTableModel(TableModel tableModel)
    {
        this();
        setTableModel(tableModel);
    }

    public SortableTableModel(TableModel tableModel, JTableHeader tableHeader)
    {
        this();
        setTableHeader(tableHeader);
        setTableModel(tableModel);
    }

    private void clearSortingState()
    {
        viewToModel = null;
        modelToView = null;
    }

    public TableModel getTableModel()
    {
        return tableModel;
    }

    public void setTableModel(TableModel tableModel)
    {
        if (this.tableModel != null)
        {
            this.tableModel.removeTableModelListener(tableModelListener);
        }

        this.tableModel = tableModel;
        if (this.tableModel != null)
        {
            this.tableModel.addTableModelListener(tableModelListener);
        }

        clearSortingState();
        fireTableStructureChanged();
    }

    public JTableHeader getTableHeader()
    {
        return tableHeader;
    }

    public void setTableHeader(JTableHeader tableHeader)
    {
        if (this.tableHeader != null)
        {
            this.tableHeader.removeMouseListener(mouseListener);
            TableCellRenderer defaultRenderer = this.tableHeader.getDefaultRenderer();
            if (defaultRenderer instanceof SortableHeaderRenderer)
                this.tableHeader.setDefaultRenderer(((SortableHeaderRenderer) defaultRenderer).tableCellRenderer);
        }

        this.tableHeader = tableHeader;

        if (this.tableHeader != null)
        {
            this.tableHeader.addMouseListener(mouseListener);
            this.tableHeader.setDefaultRenderer(new SortableHeaderRenderer(this.tableHeader.getDefaultRenderer()));
        }
    }

    public boolean isSorting()
    {
        return sortingColumns.size() != 0;
    }

    private Directive getDirective(int column)
    {
        for (int i = 0; i < sortingColumns.size(); i++)
        {
            Directive directive = (Directive) sortingColumns.get(i);
            if (directive.column == column)
                return directive;
        }
        return EMPTY_DIRECTIVE;
    }

    public int getSortingStatus(int column)
    {
        return getDirective(column).direction;
    }

    private void sortingStatusChanged()
    {
        clearSortingState();
        fireTableDataChanged();
        if (tableHeader != null)
            tableHeader.repaint();
    }

    public void setSortingStatus(int column, int status)
    {
        Directive directive = getDirective(column);
        if (directive != EMPTY_DIRECTIVE)
        {
            sortingColumns.remove(directive);
        }
        if (status != NOT_SORTED)
        {
            sortingColumns.add(new Directive(column, status));
        }
        sortingStatusChanged();
    }

    protected class ScaledIcon implements Icon
    {
        private Icon icon;
        private double factor;

        public ScaledIcon(Icon icon, double factor)
        {
            this.icon = icon;
            this.factor = factor;
        }

        public int getIconWidth()
        {
            return (int) (icon.getIconWidth() * factor);
        }

        public int getIconHeight()
        {
            return (int) (icon.getIconHeight() * factor);
        }

        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.translate(x, y);
            g2d.scale(factor, factor);
            icon.paintIcon(c, g2d, 0, 0);
            g2d.dispose();
        }
    }


    protected Icon getHeaderRendererIcon(int column, int size)
    {
        Directive directive = getDirective(column);

        if (directive == EMPTY_DIRECTIVE)
        {
            return null;
        }

        Icon icon = null;

        if (directive.direction == DESCENDING)
        {
            if (tableModel.getColumnClass(column) == String.class)
                icon = Utils.getImage("/de/skelton/images/sort_az_ascending.png");
            else
                icon = Utils.getImage("/de/skelton/images/sort_descending.png");
        }
        else if (directive.direction == ASCENDING)
        {
            if (tableModel.getColumnClass(column) == String.class)
                icon = Utils.getImage("/de/skelton/images/sort_az_descending.png");
            else
                icon = Utils.getImage("/de/skelton/images/sort_ascending.png");
        }

        if( icon != null)
            icon = new ScaledIcon(icon, 0.5 + Math.min(0.5, Math.pow(0.5, sortingColumns.indexOf(directive))));

        return icon;
    }

    private void cancelSorting()
    {
        sortingColumns.clear();
        sortingStatusChanged();
    }

    public void setColumnComparator(Class type, Comparator comparator)
    {
        if (comparator == null)
        {
            columnComparators.remove(type);
        }
        else
        {
            columnComparators.put(type, comparator);
        }
    }


    protected Comparator getComparator(int column)
    {
        Class columnType = tableModel.getColumnClass(column);

        Comparator comparator = (Comparator) columnComparators.get(columnType);

        if (comparator != null)
        {
            return comparator;
        }

        if (Comparable.class.isAssignableFrom(columnType))
        {
            return COMPARABLE_COMAPRATOR;
        }

        return LEXICAL_COMPARATOR;
    }


    private Row[] getViewToModel()
    {
        if (viewToModel == null)
        {
            int tableModelRowCount = tableModel.getRowCount();
            viewToModel = new Row[tableModelRowCount];
            for (int row = 0; row < tableModelRowCount; row++)
            {
                viewToModel[row] = new Row(row);
            }

            if (isSorting())
            {
                Arrays.sort(viewToModel);
            }
        }
        return viewToModel;
    }

    public int modelIndex(int viewIndex)
    {
        return getViewToModel()[viewIndex].modelIndex;
    }

    private int[] getModelToView()
    {
        if (modelToView == null)
        {
            int n = getViewToModel().length;
            modelToView = new int[n];
            for (int i = 0; i < n; i++)
            {
                modelToView[modelIndex(i)] = i;
            }
        }
        return modelToView;
    }

    // TableModel interface methods

    public int getRowCount()
    {
        return (tableModel == null) ? 0 : tableModel.getRowCount();
    }

    public int getColumnCount()
    {
        return (tableModel == null) ? 0 : tableModel.getColumnCount();
    }

    public String getColumnName(int column)
    {
        return tableModel.getColumnName(column);
    }

    public Class getColumnClass(int column)
    {
        return tableModel.getColumnClass(column);
    }

    public boolean isCellEditable(int row, int column)
    {
        return tableModel.isCellEditable(modelIndex(row), column);
    }

    public Object getValueAt(int row, int column)
    {
        return tableModel.getValueAt(modelIndex(row), column);
    }

    public void setValueAt(Object aValue, int row, int column)
    {
        tableModel.setValueAt(aValue, modelIndex(row), column);
    }

    // Helper classes

    private class Row implements Comparable
    {
        private int modelIndex;

        public Row(int index)
        {
            this.modelIndex = index;
        }

        public int compareTo(Object o)
        {
            int row1 = modelIndex;
            int row2 = ((Row) o).modelIndex;

            for (Iterator it = sortingColumns.iterator(); it.hasNext();)
            {
                Directive directive = (Directive) it.next();
                int column = directive.column;
                Object o1 = tableModel.getValueAt(row1, column);
                Object o2 = tableModel.getValueAt(row2, column);

                int comparison = 0;

                // Define null less than everything, except null.
                if (o1 == null && o2 == null)
                {
                    comparison = 0;
                }
                else if (o1 == null)
                {
                    comparison = -1;
                }
                else if (o2 == null)
                {
                    comparison = 1;
                }
                else
                {
                    comparison = getComparator(column).compare(o1, o2);
                }

                if (comparison != 0)
                {
                    return directive.direction == DESCENDING ? -comparison : comparison;
                }
            }
            return 0;
        }
    }

    private class TableModelHandler implements TableModelListener
    {
        public void tableChanged(TableModelEvent e)
        {
            // If we're not sorting by anything, just pass the event along.
            if (!isSorting())
            {
                clearSortingState();
                fireTableChanged(e);
                return;
            }

            // If the table structure has changed, cancel the sorting; the
            // sorting columns may have been either moved or deleted from
            // the model.
            if (e.getFirstRow() == TableModelEvent.HEADER_ROW)
            {
                cancelSorting();
                fireTableChanged(e);
                return;
            }

            // We can map a cell event through to the view without widening
            // when the following conditions apply:
            //
            // a) all the changes are on one row (e.getFirstRow() == e.getLastRow()) and,
            // b) all the changes are in one column (column != TableModelEvent.ALL_COLUMNS) and,
            // c) we are not sorting on that column (getSortingStatus(column) == NOT_SORTED) and,
            // d) a reverse lookup will not trigger a sort (modelToView != null)
            //
            // Note: INSERT and DELETE events fail this test as they have column == ALL_COLUMNS.
            //
            // The last check, for (modelToView != null) is to see if modelToView
            // is already allocated. If we don't do this check; sorting can become
            // a performance bottleneck for applications where cells
            // change rapidly in different parts of the table. If cells
            // change alternately in the sorting column and then outside of
            // it this class can end up re-sorting on alternate cell updates -
            // which can be a performance problem for large tables. The last
            // clause avoids this problem.
            int column = e.getColumn();
            if (e.getFirstRow() == e.getLastRow()
                    && column != TableModelEvent.ALL_COLUMNS
                    && getSortingStatus(column) == NOT_SORTED
                    && modelToView != null)
            {
                int viewIndex = getModelToView()[e.getFirstRow()];
                fireTableChanged(new TableModelEvent(SortableTableModel.this,
                                                     viewIndex, viewIndex,
                                                     column, e.getType()));
                return;
            }

            // Something has happened to the data that may have invalidated the row order.
            clearSortingState();
            fireTableDataChanged();
            return;
        }
    }

    private class MouseHandler extends MouseAdapter
    {
        public void mouseClicked(final MouseEvent e)
        {
            JTableHeader h = (JTableHeader) e.getSource();
            TableColumnModel columnModel = h.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            final int column = columnModel.getColumn(viewColumn).getModelIndex();
            if (column != -1)
            {
                int status = getSortingStatus(column);
                if (!e.isControlDown())
                {
                    cancelSorting();
                }
                // Cycle the sorting states through {NOT_SORTED, ASCENDING, DESCENDING} or
                // {NOT_SORTED, DESCENDING, ASCENDING} depending on whether shift is pressed.
                status = status + (e.isShiftDown() ? -1 : 1);
                status = (status + 4) % 3 - 1; // signed mod, returning {-1, 0, 1}
                setSortingStatus(column, status);
            }
        }
    }


    public class SortableHeaderRenderer implements TableCellRenderer
    {
        private TableCellRenderer tableCellRenderer;

        public SortableHeaderRenderer(TableCellRenderer tableCellRenderer)
        {
            this.tableCellRenderer = tableCellRenderer;
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column)
        {
            Component c = tableCellRenderer.getTableCellRendererComponent(table,
                                                                          value,
                                                                          isSelected,
                                                                          hasFocus,
                                                                          row,
                                                                          column);
            if (c instanceof JLabel)
            {
                JLabel l = (JLabel) c;
                l.setHorizontalTextPosition(JLabel.RIGHT);
                int modelColumn = table.convertColumnIndexToModel(column);
                l.setIcon(getHeaderRendererIcon(modelColumn, l.getFont().getSize()));
            }

            return c;
        }
    }

    private static class Directive
    {
        private int column;
        private int direction;

        public Directive(int column, int direction)
        {
            this.column = column;
            this.direction = direction;
        }
    }
}
