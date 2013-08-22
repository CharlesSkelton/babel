/***********************************************************************************************************************
 *
 * Author:    Charlie Skelton, Skelton Consulting GmbH
 * Copyright: Skelton Consulting GmbH, Stuttgart, Germany
 * License:   Creative Commons, Attribution-ShareAlike 2.0
 *            see http://creativecommons.org/licenses/by-sa/2.0/legalcode
 *            or the included license.txt file for full license details
 */

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.TimeZone;
import kx.c;

public class LineChart
{
    public LineChart(JTable table)
    {
        final JFrame frame = new JFrame("jshow Chart");

        final JFreeChart chart = createDataset(table);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setMouseZoomable(true, false);
        frame.setContentPane(chartPanel);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.setIconImage(Toolkit.getDefaultToolkit().createImage(LineChart.class.getResource("de/skelton/images/chart.png")));

        frame.pack();
        frame.setVisible(true);
        frame.requestFocus();
        frame.toFront();
    }

    private static JFreeChart createDataset(JTable table)
    {
        TimeZone tz= TimeZone.getTimeZone("GMT");

        XYDataset ds = null;

        if (table.getColumnCount() > 0)
        {
            Class klass = table.getColumnClass(0);

            if ((klass == Date.class)
                    || (klass == Time.class)
                    || (klass == c.Month.class)
                    || (klass == c.Minute.class)
                    || (klass == c.Second.class)
                    || (klass == Timestamp.class))
            {
                TimeSeriesCollection tsc = new TimeSeriesCollection();

                for (int col = 1; col < table.getColumnCount(); col++)
                {
                    TimeSeries series = null;

                    try
                    {
                        if (klass == Date.class)
                        {
                            series = new TimeSeries(table.getColumnName(col), Day.class);

                            for (int row = 0; row < table.getRowCount(); row++)
                            {
                                Date date = (Date) table.getValueAt(row, 0);
                                Day day = new Day(date, tz);

                                Object o = table.getValueAt(row, col);
                                if (o instanceof Number)
                                {
                                    ((TimeSeries) series).addOrUpdate(day, (Number) table.getValueAt(row, col));
                                }
                            }
                        }
                        else if (klass == Time.class)
                        {
                            series = new TimeSeries(table.getColumnName(col), Millisecond.class);

                            for (int row = 0; row < table.getRowCount(); row++)
                            {
                                Time time = (Time) table.getValueAt(row, 0);
                                Millisecond ms = new Millisecond(time, tz);

                              //    Millisecond ms = new Millisecond(time);
                                Object o = table.getValueAt(row, col);
                                if (o instanceof Number)
                                {
                                    series.addOrUpdate(ms, (Number) table.getValueAt(row, col));
                                }
                            }
                        }
                        else if (klass == Timestamp.class)
                        {
                            series = new TimeSeries(table.getColumnName(col), Millisecond.class);

                            for (int row = 0; row < table.getRowCount(); row++)
                            {
                                Timestamp time = (Timestamp) table.getValueAt(row, 0);
                                Millisecond ms = new Millisecond(time, tz);

                                Object o = table.getValueAt(row, col);
                                if (o instanceof Number)
                                {
                                    series.addOrUpdate(ms, (Number) table.getValueAt(row, col));
                                }

                            }
                        }
                        else if (klass == c.Month.class)
                        {
                            series = new TimeSeries(table.getColumnName(col), Month.class);

                            for (int row = 0; row < table.getRowCount(); row++)
                            {
                                c.Month time = (c.Month) table.getValueAt(row, 0);
                                int m = time.i + 24000;
                                int y = m / 12;
                                m = 1 + m % 12;

                                Month month = new Month(m, y);

                                Object o = table.getValueAt(row, col);
                                if (o instanceof Number)
                                {
                                    series.addOrUpdate(month, (Number) table.getValueAt(row, col));
                                }

                            }
                        }
                        else if (klass == c.Second.class)
                        {
                            series = new TimeSeries(table.getColumnName(col), Second.class);

                            for (int row = 0; row < table.getRowCount(); row++)
                            {
                                c.Second time = (c.Second) table.getValueAt(row, 0);

                                Second second = new Second(time.i % 60, time.i / 60, 0, 1, 1, 2001);

                                Object o = table.getValueAt(row, col);
                                if (o instanceof Number)
                                {
                                    series.addOrUpdate(second, (Number) table.getValueAt(row, col));
                                }

                            }
                        }
                        else if (klass == c.Minute.class)
                        {
                            series = new TimeSeries(table.getColumnName(col), Minute.class);

                            for (int row = 0; row < table.getRowCount(); row++)
                            {
                                c.Minute time = (c.Minute) table.getValueAt(row, 0);

                                Minute minute = new Minute(time.i % 60, time.i / 60, 1, 1, 2001);

                                Object o = table.getValueAt(row, col);
                                if (o instanceof Number)
                                {
                                    series.addOrUpdate(minute, (Number) table.getValueAt(row, col));
                                }
                            }
                        }
                    }
                    catch (SeriesException e)
                    {
                        System.err.println("Error adding to series");
                    }

                    if (series.getItemCount() > 0)
                        tsc.addSeries(series);
                }

                ds = tsc;
            }
            else if ((klass == double.class)
                    || (klass == int.class)
                    || (klass == short.class)
                    || (klass == long.class)
                    || (klass == Time.class))
            {
                XYSeriesCollection xysc = new XYSeriesCollection();

                for (int col = 1; col < table.getColumnCount(); col++)
                {
                    XYSeries series = null;

                    try
                    {
                        series = new XYSeries(table.getColumnName(col));

                        for (int row = 0; row < table.getRowCount(); row++)
                        {
                            Number x = (Number) table.getValueAt(row, 0);

                            Object y = table.getValueAt(row, col);
                            if (y instanceof Number)
                            {
                                series.add(x, (Number) y);
                            }
                        }
                    }
                    catch (SeriesException e)
                    {
                        System.err.println("Error adding to series");
                    }

                    if (series.getItemCount() > 0)
                        xysc.addSeries(series);
                }

                ds = xysc;
            }
        }

        if( ds != null)
        {
        boolean legend = false;

        if (ds.getSeriesCount() > 1)
        {
            legend = true;
        }

        if( ds instanceof XYSeriesCollection)
        {
            return ChartFactory.createXYLineChart("",
                                                  "",
                                                  "",
                                                  ds,
                                                  PlotOrientation.VERTICAL,
                                                  legend,
                                                  true,
                                                  true);
        }
        else if( ds instanceof TimeSeriesCollection)
            return ChartFactory.createTimeSeriesChart("",
                                                      "",
                                                      "",
                                                      ds,
                                                      legend,
                                                      true,
                                                      true);
        }

            return null;
    }
}
