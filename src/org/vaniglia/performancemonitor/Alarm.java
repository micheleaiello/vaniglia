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

/**
 * Alarm class.
 */
public class Alarm {

    private AlarmTask alarmTask;
    private double threshold;
    private boolean greater;

    private Lap lap;

    /**
     * Default constructor
     *
     * @param alarmTask the task to execute in case the alarm has to go off.
     * @param threshold the alarm threshold.
     * @param greater if true the alarm is raised if the input value is greater than the thresold,
     *              if false the alarm is raised for values below the threshold.
     */
    public Alarm(AlarmTask alarmTask, double threshold, boolean greater) {
        this.alarmTask = alarmTask;
        this.threshold = threshold;
        this.greater = greater;
    }

    /**
     * Sets the alarm threshold.
     *
     * This method is usefull when the alarm threshold can change during the application lifetime.
     *
     * @param newThreshold the new threshold for the alarm.
     */
    public void setThreshold(double newThreshold) {
        this.threshold = newThreshold;
    }

    /**
     * Sets the lap associated with the alarm.
     *
     * @param lap the lap to associate with the alarm
     */
    void setLap(Lap lap) {
        this.lap = lap;
    }

    /**
     * Returns the lap associated with the alarm.
     *
     * @return the alarm's Lap.
     */
    public Lap getLap() {
        return lap;
    }

    /**
     * Returns the alarm's threshold.
     *
     * @return the alarm's threshold
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Checks if the alarm has to be raised.
     *
     * @param value input value to check agains the thresold.
     *
     * @return true if the alarm has been raised and the alarm task has been called.
     */
    public boolean check(double value) {
        if (greater) {
            if (value >= threshold) {
                alarmTask.run(this);
                return true;
            }
        }
        else {
            if (value <= threshold) {
                alarmTask.run(this);
                return true;
            }
        }

        return false;
    }

}
