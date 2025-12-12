/**
 * Project Vaniglia
 * User: Michele Aiello
 *
 * Copyright (C) 2003/2007  Michele Aiello
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.vaniglia.performancemonitor;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Lap class.
 */
public class Lap {

    NumberFormat nf = NumberFormat.getInstance();

    /**
     * Lap Name.
     */
    private String name;

    /**
     * Lap period.
     */
    private long lapPeriod;

    private boolean lapDelayed;

    // t(x-1)
    private long txm1;

    // t(x)
    private long tx;

    private long lapEvents;
    private long lapTotalLatencies;
    private double lapMaxLatency;

    private long lapEventsAccumulator;
    private long lapLatenciesAccumulator;
    private double lapMaxLatencyAccumulator;

    private final Vector eventAlarms = new Vector();
    private final Vector rateAlarms = new Vector();
    private final Vector avgLatencyAlarms = new Vector();
    private final Vector maxLatencyAlarms = new Vector();

    private LapHistory lapHistory;
    private boolean totalOnHistory = false;

    private Timer lapTimer;

    private Logger logger;
    private String loggerPrefix;
    private Level level;

    private LapAction lapAction;

    private PerformanceMonitor monitor;

    private static class LapTimerTask extends TimerTask {
        private Lap lap;

        public LapTimerTask(Lap lap) {
            this.lap = lap;
        }

        public void run() {
            lap.lap(System.currentTimeMillis());
            LapAction action = lap.getLapAction();
            if (action != null) {
                action.execute(lap);
            }
        }
    }

    /**
     * Defaul constructor.
     * Only PerformanceMonitor is in charge of creating and managing Lap objects, and so
     * is is a Factory for this class.
     *
     * @param name the Lap Name
     * @param t0 lap initial time
     * @param period lap period
     * @param delayed if true, the first period duration will be (period - currentTimeMillis%period).
     * @param historyDepth lap history depth
     * @param action the lap lapAction
     * @param monitor the lap monitor
     */
    Lap(String name, long t0, long period, boolean delayed, int historyDepth, LapAction action, PerformanceMonitor monitor) {
        this.name = name;
        this.monitor = monitor;

        lapEvents = 0;
        lapEventsAccumulator = 0;
        lapLatenciesAccumulator = 0;
        lapPeriod = period;
        lapDelayed = delayed;

        if (historyDepth > 0) {
            this.lapHistory = new LapHistory(historyDepth);
        }

        this.lapAction = action;

        start(t0);
    }

    /**
     * This method sets a logger (log4j) for the Lap.
     * If a logger is setted the Lap will write to the logger any lap with INFO priority.
     *
     * @param logger the logger to set for this lap.
     * @param level the log level for this lap.
     * @param loggerPrefix the logger prefix string.
     */
    public void setLogger(Logger logger, Level level, String loggerPrefix) {
        this.logger = logger;
        this.level = level;
        this.loggerPrefix = loggerPrefix;
    }

    public void setTotalRateOnHistory(boolean value) {
        totalOnHistory = value;
    }

    /**
     * Starts the lap.
     *
     * @param timeMillis lap initial time
     */
    final void start(long timeMillis) {
        tx = timeMillis;
        txm1 = timeMillis;

        if (lapTimer != null) {
            lapTimer.cancel();
        }

        LapTimerTask timerTask = new LapTimerTask(this);

        lapTimer = new Timer(true);
        if (lapDelayed) {
            lapTimer.scheduleAtFixedRate(timerTask, lapPeriod-(System.currentTimeMillis()%lapPeriod), lapPeriod);
        }
        else {
            lapTimer.scheduleAtFixedRate(timerTask, lapPeriod, lapPeriod);
        }
    }

    /**
     * Restarts the lap.
     *
     * @param timeMillis the lap initial time
     */
    final void restart(long timeMillis) {
        if (lapTimer != null) {
            lapTimer.cancel();
        }
        lapTimer = null;
        start(timeMillis);
    }

    /**
     * Updates the lap with n news events.
     *
     * @param events number of new events
     * @param latencies total latencies
     * @param maxlatency max latancy
     */
    final synchronized void update(long events, long latencies, double maxlatency) {
        lapEventsAccumulator += events;
        lapLatenciesAccumulator += latencies;
        if (maxlatency > lapMaxLatencyAccumulator) {
            lapMaxLatencyAccumulator = maxlatency;
        }
    }

    /**
     * Closes a "lap".
     * During this operation the Lap updates his history and logger (if any), and checks all his alarms.
     *
     * @param timeMillis lap closure time.
     */
    final void lap(long timeMillis) {
        monitor.applyPendingUpdates();

        txm1 = tx;
        tx = timeMillis;

        lapEvents = lapEventsAccumulator;
        lapTotalLatencies = lapLatenciesAccumulator;
        lapMaxLatency = lapMaxLatencyAccumulator;

        lapEventsAccumulator = 0;
        lapLatenciesAccumulator = 0;
        lapMaxLatencyAccumulator = 0;

        if ((lapHistory != null) && ((tx - txm1) > 0)) {
            LapHistoryRecord historyRecord = new LapHistoryRecord(txm1, tx - txm1, lapTotalLatencies, lapMaxLatency, lapEvents);
            if (totalOnHistory) {
                double totalRate = monitor._getRate();
                historyRecord.setTotalRate(totalRate);

                double totalAvgLat = monitor._getTotalAverageLatency();
                historyRecord.setTotalAverageLatency(totalAvgLat);
            }
            lapHistory.addRecord(historyRecord);
        }

        if (logger != null) {
            StringBuffer buff = new StringBuffer(200);

            buff.append(loggerPrefix);
            buff.append(':');
            buff.append(name);
            buff.append(": - Start: ");
            if (PerformanceMonitor.getDateFormat() != null) {
                buff.append(PerformanceMonitor.getDateFormat().format(new Date(txm1)));
            }
            else {
                buff.append(txm1);
            }
            buff.append(" - Elapsed: ");
            buff.append(nf.format(getElapsed()));
            buff.append(" - Events: ");
            buff.append(nf.format(getEvents()));
            buff.append(" - Rate: ");
            buff.append(nf.format(getRate() * 1000));
            buff.append(" e/s");
            buff.append(" - Avg Lat.: ");
            buff.append(nf.format(getAverageLatency()));
            buff.append(" ms");
            buff.append(" - Max Lat.: ");
            buff.append(nf.format(getMaxLatency()));
            buff.append(" ms");

            logger.log(level, buff.toString());
        }

        synchronized (eventAlarms) {
            int eventAlarmsSize = eventAlarms.size();
            for (int i = 0; i < eventAlarmsSize; i++) {
                Alarm alarm = (Alarm) eventAlarms.get(i);
                alarm.check(lapEvents);
            }
        }

        synchronized (rateAlarms) {
            int rateAlarmsSize = rateAlarms.size();
            double rate = getRate();
            for (int i = 0; i < rateAlarmsSize; i++) {
                Alarm alarm = (Alarm) rateAlarms.get(i);
                alarm.check(rate);
            }
        }

        synchronized(avgLatencyAlarms) {
            int avgLatAlarmsSize = avgLatencyAlarms.size();
            double avgLat = getAverageLatency();
            for (int i = 0; i < avgLatAlarmsSize; i++) {
                Alarm alarm = (Alarm) avgLatencyAlarms.get(i);
                alarm.check(avgLat);
            }
        }

        synchronized(maxLatencyAlarms) {
            int maxLatAlarmsSize = maxLatencyAlarms.size();
            double maxLat = getMaxLatency();
            for (int i = 0; i < maxLatAlarmsSize; i++) {
                Alarm alarm = (Alarm) maxLatencyAlarms.get(i);
                alarm.check(maxLat);
            }
        }
    }

    /**
     * Stops the Lap.
     *
     * @param timeMillis lap stopping time.
     */
    final void stop(long timeMillis) {
        lapTimer.cancel();
        lapTimer = null;
    }

    /**
     * Adds an event alarm to the lap.
     * Events alarms are raised based on the number of processed events in the lap period.
     *
     * @param alarm the event alarm
     */
    public final void addEventsAlarm(Alarm alarm) {
        alarm.setLap(this);
        synchronized (eventAlarms) {
            eventAlarms.add(alarm);
        }
    }

    /**
     * Adds a rate alarm to the lap.
     * Rate alarms are raised based on the current processing rate in the lap period.
     *
     * @param alarm the rate alarm
     */
    public final void addRateAlarm(Alarm alarm) {
        alarm.setLap(this);
        synchronized (rateAlarms) {
            rateAlarms.add(alarm);
        }
    }

    /**
     * Adds an average latency alarm to the lap.
     * Average latency alarms are raised based on the average latency in the lap period.
     *
     * @param alarm the average latency alarm
     */
    public final void addAverageLatencyAlarm(Alarm alarm) {
        alarm.setLap(this);
        synchronized(avgLatencyAlarms) {
            avgLatencyAlarms.add(alarm);
        }
    }

    /**
     * Adds a max latency alarm to the lap.
     * Max latency alamrs are raised based on the max latency in the lap period.
     *
     * @param alarm the max latency alarm
     */
    public final void addMaxLatencyAlarm(Alarm alarm) {
        alarm.setLap(this);
        synchronized(maxLatencyAlarms) {
            maxLatencyAlarms.add(alarm);
        }
    }

    public String getName() {
        return name;
    }

    public final long getElapsed() {
        return tx - txm1;
    }

    public final long getEvents() {
        return lapEvents;
    }

    public final long getTotalLatencies() {
        return lapTotalLatencies;
    }

    public double getMaxLatency() {
        return lapMaxLatency;
    }

    public final double getAverageLatency() {
        if (lapEvents != 0) {
            return (double)lapTotalLatencies/(double)lapEvents;
        }
        else {
            return 0;
        }
    }

    /**
     * Returns the lap rate (events/msecs)
     * @return average number of events per millisecond for the lap.
     */
    public final double getRate() {
        if ((tx - txm1) > 0) {
            return (double) lapEvents / (double) (tx - txm1);
        }
        else {
            return 0.0;
        }
    }

    public final long getPeriod() {
        return lapPeriod;
    }

    public boolean isLapDelayed() {
        return lapDelayed;
    }

    public final LapHistory getLapHistory() {
        return lapHistory;
    }

    LapAction getLapAction() {
        return lapAction;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append("LapName: ");
        buff.append(name);
        buff.append(" - Start: ");
        if (PerformanceMonitor.getDateFormat() != null) {
            buff.append(PerformanceMonitor.getDateFormat().format(new Date(txm1)));
        }
        else {
            buff.append(txm1);
        }
        buff.append(" - Elapsed: ");
        buff.append(nf.format(tx - txm1));
        buff.append(" - Events: ");
        buff.append(nf.format(lapEvents));
        buff.append(" - Rate: ");
        buff.append(nf.format(getRate() * 1000));
        buff.append(" e/s");
        buff.append(" - Avg Lat.: ");
        buff.append(nf.format(getAverageLatency()));
        buff.append(" ms");
        buff.append(" - Max Lat.: ");
        buff.append(nf.format(getMaxLatency()));
        buff.append(" ms");

        return buff.toString();

    }
}
