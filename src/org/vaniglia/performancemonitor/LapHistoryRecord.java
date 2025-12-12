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

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Date;

/**
 * Lap History Record
 */
public class LapHistoryRecord implements Serializable {

    private static final NumberFormat nf = NumberFormat.getInstance();

    private long startTime;
    private long elapsed;
    private long latency;
    private double maxLatency;
    private long events;

    private double totalRate;
    private double totalAvgLat;

    public LapHistoryRecord(long startTime, long elapsed, long latency, double maxLatency, long events) {
        this.startTime = startTime;
        this.elapsed = elapsed;
        this.latency = latency;
        this.maxLatency = maxLatency;
        this.events = events;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getElapsed() {
        return elapsed;
    }

    public long getLatency() {
        return latency;
    }

    public double getMaxLatency() {
        return maxLatency;
    }

    public long getEvents() {
        return events;
    }

    public double getRate() {
        if (elapsed > 0) {
            return (double) events / (double) (elapsed);
        }
        else {
            return 0;
        }
    }

    public double getAverageLatency() {
        if (events > 0) {
            return ((double) (latency))/((double)(events));
        }
        else {
            return 0;
        }
    }

    public double getTotalRate() {
        return totalRate;
    }

    public void setTotalRate(double totalRate) {
        this.totalRate = totalRate;
    }

    public double getTotalAvgLat() {
        return totalAvgLat;
    }

    public void setTotalAverageLatency(double totalAvgLat) {
        this.totalAvgLat = totalAvgLat;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append("Start: ");
        if (PerformanceMonitor.getDateFormat() != null) {
            buff.append(PerformanceMonitor.getDateFormat().format(new Date(startTime)));
        }
        else {
            buff.append(startTime);
        }
        buff.append(" - Elapsed: ");
        buff.append(nf.format(elapsed));
        buff.append(" ms");
        buff.append(" - Events: ");
        buff.append(nf.format(events));
        buff.append(" - Rate: ");
        buff.append(nf.format(getRate() * 1000));
        buff.append(" e/s");

        if (totalRate != 0) {
            buff.append(" (TotalRate: ");
            buff.append(nf.format(totalRate * 1000));
            buff.append(" e/s)");
        }

        buff.append(" - Avg Latency: ");
        buff.append(nf.format(getAverageLatency()));
        buff.append(" ms");

        if (totalAvgLat != 0) {
            buff.append(" (TotalAvgLat: ");
            buff.append(nf.format(totalAvgLat));
            buff.append(" ms)");
        }

        buff.append(" - Max Latency: ");
        buff.append(nf.format(maxLatency));
        buff.append(" ms");

        return buff.toString();
    }
}
