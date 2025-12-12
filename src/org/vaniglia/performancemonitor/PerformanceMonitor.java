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

import org.apache.commons.collections.FastHashMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.PrintStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The Performance Monitor is a utility class used to keep track of performances of your applications.
 */
public class PerformanceMonitor {

    /**
     * Instances map.
     */
    private static FastHashMap _instances;

    // t0
    private long t0 = 0;

    // Events handled from t0.
    private long events = 0;

    // Sum of all latencies
    private long totalLatencies = 0;

    // Max latency. It's a double because in cases where the update is provided for multiple events
    // an average value is used as max of that batch.
    private double maxLatency = 0;

    // Stop time. The time the monitor has been stopped;
    private long stopTime = 0;

    // State of the monitor.
    private boolean running = false;

    private ThreadLocal pendingUpdates = new ThreadLocal();
    private final ArrayList pendingList = new ArrayList(16);

    public static SimpleDateFormat dateFormat = null;

    private class MutableLong {
        long theLong;

        public MutableLong() {
            this.theLong = 0;
        }

        public synchronized void setLong(long value) {
            theLong = value;
        }

        public long getLong() {
            return theLong;
        }

        public synchronized void increment(long n) {
            this.theLong += n;
        }

        /**
         * This method "pops" the value from the counter and reset it internal value to 0.
         *
         * @return the counter value.
         */
        public synchronized long pop() {
            long previousValue = theLong;
            theLong = 0;
            return previousValue;
        }
    }

    private class Modifications {
        private MutableLong events;
        private MutableLong totaleLatency;
        private double maxLatency;

        public Modifications() {
            events = new MutableLong();
            totaleLatency = new MutableLong();
            maxLatency = 0.0;
        }

        public MutableLong getEvents() {
            return events;
        }

        public MutableLong getTotaleLatency() {
            return totaleLatency;
        }

        public double getMaxLatency() {
            return maxLatency;
        }

        public void setMaxLatency(double maxLatency) {
            this.maxLatency = maxLatency;
        }
    }

    /**
     * Map of all the monitor's lap.
     */
    private HashMap _laps = new HashMap();

    private final NumberFormat nf = NumberFormat.getInstance();

    static {
        _instances = new FastHashMap();
        _instances.setFast(true);
    }

    /**
     * This method is used to get a "named" instance of PerformanceMonitor.
     * If the instance doesn't yet exist it is created.
     *
     * @param name the name of the isntance.
     *
     * @return the instance with the given name or a newly created one.
     */
    public static synchronized PerformanceMonitor getInstance(String name) {
        PerformanceMonitor perfmon = (PerformanceMonitor) _instances.get(name);
        if (perfmon == null) {
            perfmon = new PerformanceMonitor();
            _instances.put(name, perfmon);
        }
        return perfmon;
    }

    /**
     * Default constructor.
     */
    public PerformanceMonitor() {
        t0 = System.currentTimeMillis();
        events = 0;
        totalLatencies = 0;
        maxLatency = 0;
        stopTime = -1;
    }

    /**
     * Starts the monitor.
     */
    public final synchronized void start() {
        running = true;
        t0 = System.currentTimeMillis();
        stopTime = -1;
        Collection laps = _laps.values();
        Iterator it = laps.iterator();
        while (it.hasNext()) {
            Lap lap = (Lap) it.next();
            lap.start(t0);
        }
    }

    /**
     * Resets the monitor.
     */
    public final synchronized void restart() {
        applyPendingUpdates();

        start();
        events = 0;
        totalLatencies = 0;
        maxLatency = 0;
        stopTime = -1;

        Collection laps = _laps.values();
        Iterator it = laps.iterator();
        while (it.hasNext()) {
            Lap lap = (Lap) it.next();
            lap.restart(t0);
        }
    }

    /**
     * Adds a lap to the Monitor.
     * A lap is a timer that maintain statistics for the given time period.
     * A lap can be configured to keep track of his history using the historyDepth parameter.
     *
     * @param lapName the name of the lap
     * @param period the lap period in milliseconds
     * @param delayed if true, the first period will be (period - currentTimeMillis % period)
     * @param historyDepth the lap history depth. 0 for no history
     *
     * @return the newly created lap
     *
     * @throws PerformanceMonitorException if the lap already exists.
     */
    public final synchronized Lap addLap(String lapName, long period, boolean delayed, int historyDepth) throws PerformanceMonitorException {
        return addLap(lapName, period, delayed, historyDepth, null);
    }

    /**
     * Adds a lap with an associated action to the Monitor.
     * A lap is a timer that maintain statistics for the given time period.
     * A lap can be configured to keep track his history using the historyDepth parameter.
     *
     * @param lapName the name of the lap
     * @param period the lap period in milliseconds
     * @param delayed if true, the first period will be (period - currentTimeMillis % period)
     * @param historyDepth the lap history depth. 0 for no history
     * @param action the lap action
     *
     * @return the newly created lap
     *
     * @throws PerformanceMonitorException if the lap already exists.
     */
    public final synchronized Lap addLap(String lapName, long period, boolean delayed, int historyDepth, LapAction action) throws PerformanceMonitorException {
        Lap lap = (Lap) _laps.get(lapName);
        if (lap == null) {
            lap = new Lap(lapName, t0, period, delayed, historyDepth, action, this);
            _laps.put(lapName, lap);
        }
        else {
            throw new PerformanceMonitorException("Lap '"+lapName+"' already existent.");
        }
        return lap;
    }

    /**
     * Adds an event alarm for a given lap of the monitor.
     * An event alarm is an alarm based on the numeber of events received by a lap in his time period.
     * If the alarm threshold is exceeded than the alarm task related to the alarm is called.
     *
     * @param lapName the name of the lap you want to configure with the given alarm
     * @param alarm the alarm
     *
     * @throws PerformanceMonitorException if the lap doesn't exists.
     */
    public final synchronized void addEventAlarm(String lapName, Alarm alarm) throws PerformanceMonitorException {
        Lap lap = (Lap) _laps.get(lapName);
        if (lap != null) {
            lap.addEventsAlarm(alarm);
        }
        else {
            throw new PerformanceMonitorException("Lap '"+lapName+"' doesn't existent.");
        }
    }

    /**
     * Adds a rate alarm for a given lap of the monitor.
     * A rate alarm is an alarm based on the average event/msec received by a lap in his time period.
     * If the alarm threshold is exceeded than the alarm task related to the alarm is called.
     *
     * @param lapName the name of the lap you want to configure with the given alarm
     * @param alarm the alarm
     *
     * @throws PerformanceMonitorException if the lap doesn't exists.
     */
    public final synchronized void addRateAlarm(String lapName, Alarm alarm) throws PerformanceMonitorException {
        Lap lap = (Lap) _laps.get(lapName);
        if (lap != null) {
            lap.addRateAlarm(alarm);
        }
        else {
            throw new PerformanceMonitorException("Lap '"+lapName+"' doesn't existent.");
        }
    }

    /**
     * Adds an average latency alarm for a given lap of the monitor.
     * An average latency alarm is an alarm based on the average latency mesured by the lap in his time period.
     * If the alarm threshold is exceed than the alarm task related to the alarm is called.
     *
     * @param lapName the name of the lap you want to configure with the given alarm
     * @param alarm the alarm
     *
     * @throws PerformanceMonitorException if the lap doesn't exists.
     */
    public final synchronized void addAverageLatencyAlarm(String lapName, Alarm alarm) throws PerformanceMonitorException {
        Lap lap = (Lap) _laps.get(lapName);
        if (lap != null) {
            lap.addAverageLatencyAlarm(alarm);
        }
        else {
            throw new PerformanceMonitorException("Lap '"+lapName+"' doesn't existent.");
        }
    }

    /**
     * Adds a max latency alarm for a given lap of the monitor.
     * A Max latency alarm is an alarm based on the max latency mesured by the lap in his time period.
     * If the alarm threshold is exceed than the alarm task related to the alarm is called.
     *
     * @param lapName the name of the lap you want to configure with the given alarm
     * @param alarm the alarm
     *
     * @throws PerformanceMonitorException if the lap doesn't exists.
     */
    public final synchronized void addMaxLatencyAlarm(String lapName, Alarm alarm) throws PerformanceMonitorException {
        Lap lap = (Lap) _laps.get(lapName);
        if (lap != null) {
            lap.addMaxLatencyAlarm(alarm);
        }
        else {
            throw new PerformanceMonitorException("Lap '"+lapName+"' doesn't existent.");
        }
    }

    /**
     * Updates the monitor with n events.
     *
     * @param events the number of events handled.
     *
     * @deprecated this method assumes a latency of 0ms for the calls.
     * The new update method (with events and latencies parameters) should be used.
     */
    public final void update(long events) {
        update(events, 0);
    }

    /**
     * Updates the monitor with n events.
     *
     * @param events the number of events handled.
     * @param latencies the total latencies for the events on update.
     */
    public final void update(long events, long latencies) {
        Modifications currPendingModifications = (Modifications) pendingUpdates.get();
        if (currPendingModifications == null) {
            currPendingModifications = new Modifications();
            pendingUpdates.set(currPendingModifications);
            synchronized (pendingList) {
                pendingList.add(currPendingModifications);
            }
        }
        currPendingModifications.getEvents().increment(events);
        currPendingModifications.getTotaleLatency().increment(latencies);
        double currentMax = currPendingModifications.getMaxLatency();
        double eventMax = (double)latencies / (double)events;
        if (eventMax > currentMax) {
            currPendingModifications.setMaxLatency(eventMax);
        }
    }

    /**
     * This method applies all pending updates to the monitor and to all the laps.
     */
    protected synchronized final void applyPendingUpdates() {
        int numOfPending = pendingList.size();
        int delta = 0;
        int deltaLatency = 0;
        double deltaMaxLatency = 0;
        for (int i = 0; i < numOfPending; i++) {
            Modifications pending = (Modifications) pendingList.get(i);
            delta += pending.getEvents().pop();
            deltaLatency += pending.getTotaleLatency().pop();
            deltaMaxLatency = pending.getMaxLatency();
            pending.setMaxLatency(0);
        }

        events += delta;
        totalLatencies += deltaLatency;
        if (deltaMaxLatency > maxLatency) {
            maxLatency = deltaMaxLatency;
        }

        Collection laps = _laps.values();
        Iterator it = laps.iterator();
        while (it.hasNext()) {
            Lap lap = (Lap) it.next();
            lap.update(delta, deltaLatency, deltaMaxLatency);
        }
    }

    /**
     * Stops the monitor.
     */
    public final synchronized void stop() {
        applyPendingUpdates();
        stopTime = System.currentTimeMillis();
        Collection laps = _laps.values();
        Iterator it = laps.iterator();
        while (it.hasNext()) {
            Lap lap = (Lap) it.next();
            lap.stop(stopTime);
        }
        running = false;
    }

    /**
     * This method prints to a given PrintStream the monitor total statistics.
     *
     * @param out the PrintStream to print to.
     */
    public final void printTotals(PrintStream out) {
        applyPendingUpdates();

        long delta;

        if (stopTime > 0) {
            delta = stopTime - t0;
        }
        else {
            delta = System.currentTimeMillis() - t0;
        }

        out.println("*** Totals ***");
        out.println("Duration: " + nf.format(delta) + "ms");
        out.println("Events  : " + nf.format(events));
        out.println("Rate    : " + nf.format((double) events / (double) delta) + " KEvents/s, "
                + nf.format((double) events / ((double) delta) * 3.6) + " MEvents/h");
        if ((totalLatencies > 0) && (events != 0)) {
            out.println("Avg Lat.: "+nf.format((double)totalLatencies/(double)events)+" ms");
            out.println("Max Lat.: "+nf.format(maxLatency)+" ms");
        }
    }

    /**
     * This method prints to a given log4j Logger the monitor total statistics.
     *
     * @param logger the Logger to print to.
     * @param level the log level.
     */
    public final void printTotals(Logger logger, Level level) {
        applyPendingUpdates();

        long delta;

        if (stopTime > 0) {
            delta = stopTime - t0;
        }
        else {
            delta = System.currentTimeMillis() - t0;
        }

        logger.log(level, "*** Totals ***");
        logger.log(level, "Duration: " + nf.format(delta) + "ms");
        logger.log(level, "Events  : " + nf.format(events));
        logger.log(level, "Rate    : " + nf.format((double) events / (double) delta) + " KEvents/s, "
                + nf.format((double) events / ((double) delta) * 3.6) + " MEvents/h");
        if ((totalLatencies > 0) && (events != 0)) {
            logger.log(level, "Avg Lat.: "+nf.format((double)totalLatencies/(double)events)+" ms");
            logger.log(level, "Max Lat.: "+nf.format(maxLatency)+" ms");
        }
    }

    /**
     * This method prints to a given PrintStream a lap of the monitor.
     *
     * @param lapName the name of the lap you want to print
     * @param out the PrintStream to print to
     *
     * @throws PerformanceMonitorException if the lap doesn't exists.
     */
    public final void printLap(String lapName, PrintStream out) throws PerformanceMonitorException {
        applyPendingUpdates();

        Lap lap = (Lap) _laps.get(lapName);
        if (lap == null) {
            throw new PerformanceMonitorException("Lap '"+lapName+"' doesn't exist.");
        }
        long delta = lap.getElapsed();
        long events = lap.getEvents();
        double avgLatency = lap.getAverageLatency();
        double maxLatency = lap.getMaxLatency();
        out.println(lapName+" (Period: "+lap.getPeriod()+"ms)");
        out.println("Duration: " + nf.format(delta) + "ms");
        out.println("Events  : " + nf.format(events));
        out.println("Rate    : " + nf.format((double) events / (double) delta) + " KEvents/s, "
                + nf.format((double) events / ((double) delta) * 3.6) + " MEvents/h");
        out.println("Avg Lat.: "+nf.format(avgLatency)+" ms");
        out.println("Max Lat.: "+nf.format(maxLatency)+" ms");
    }

    /**
     * This method prints to a given PrintStream a lap of the monitor.
     *
     * @param lapName the name of the lap you want to print
     * @param logger the Logger to print to.
     * @param level the log level.
     *
     * @throws PerformanceMonitorException if the lap doesn't exists.
     */
    public final void printLap(String lapName, Logger logger, Level level) throws PerformanceMonitorException {
        applyPendingUpdates();

        Lap lap = (Lap) _laps.get(lapName);
        if (lap == null) {
            throw new PerformanceMonitorException("Lap '"+lapName+"' doesn't exist.");
        }
        long delta = lap.getElapsed();
        long events = lap.getEvents();
        double avgLatency = lap.getAverageLatency();
        double maxLatency = lap.getMaxLatency();
        logger.log(level, lapName+" (Period: "+lap.getPeriod()+"ms)");
        logger.log(level, "Duration: " + nf.format(delta) + "ms");
        logger.log(level, "Events  : " + nf.format(events));
        logger.log(level, "Rate    : " + nf.format((double) events / (double) delta) + " KEvents/s, "
                + nf.format((double) events / ((double) delta) * 3.6) + " MEvents/h");
        logger.log(level, "Avg Lat.: "+nf.format(avgLatency)+" ms");
        logger.log(level, "Max Lat.: "+nf.format(maxLatency)+" ms");
    }

    /**
     * Returns the elapsed time in milliseconds.
     *
     * @return elapsed time in milliseconds.
     */
    public final long getElapsed() {
        return System.currentTimeMillis() - t0;
    }

    /**
     * Returns the total number of events.
     *
     * @return total number of events.
     */
    public final long getNumberOfEvents() {
        applyPendingUpdates();

        return events;
    }

    /**
     * Returns the total rate (events/msecs).
     *
     * @return the average number of events per millisecond for the whole monitor execution.
     */
    public final double getRate() {
        applyPendingUpdates();

        long delta = System.currentTimeMillis() - t0;
        if (delta > 0) {
            return ((double) events / (double) delta);
        }
        else {
            return 0;
        }
    }

    /**
     * Returns the total rate (events/msecs).
     * This method doesn't apply pending update and so is for internal use only.
     *
     * @return the average number of events per millisecond for the whole monitor execution.
     */
    final double _getRate() {
        long delta = System.currentTimeMillis() - t0;
        if (delta > 0) {
            return ((double) events / (double) delta);
        }
        else {
            return 0;
        }
    }

    public double _getTotalAverageLatency() {
        if (events > 0) {
            return (double)totalLatencies/(double)events;
        }
        else {
            return 0;
        }
    }

    /**
     * Returns the lap with the given name.
     *
     * @param lapName the lap name
     *
     * @return the lap with the given name
     *
     * @throws PerformanceMonitorException if the lap doesn't exists.
     */
    public final Lap getLap(String lapName) throws PerformanceMonitorException {
        applyPendingUpdates();

        Lap lap = (Lap) _laps.get(lapName);
        if (lap != null) {
            return lap;
        }
        else {
            throw new PerformanceMonitorException("Lap '"+lapName+"' not existent.");
        }
    }

    /**
     * Sets the monitor date format.
     * This format is used everywhere a date or a time are printed out.
     *
     * @param dateFormat the date format to use for the monitor.
     */
    public static void setDateFormat(SimpleDateFormat dateFormat) {
        PerformanceMonitor.dateFormat = dateFormat;
    }

    /**
     * Returns the monitor date format currently set.
     *
     * @return the monitor date format
     */
    public static SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

}
