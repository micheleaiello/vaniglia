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
package org.vaniglia.examples.performancemonitor;

import org.vaniglia.performancemonitor.*;

import java.text.SimpleDateFormat;
import java.text.NumberFormat;

public class Main {

    public static void main(String[] args) throws PerformanceMonitorException {
        PerformanceMonitor monitor = PerformanceMonitor.getInstance("Test");
        String hundredLapName = "100 ms";
        String secLapName = "1 sec";

        AlarmTask overRateLimitAlarmTask = new AlarmTask() {
            public void run(Alarm alarm) {
                System.out.println(System.currentTimeMillis()+" - Exceeded rate limit ("+alarm.getThreshold()+" e/ms).");
                System.out.println(alarm.getLap());
                System.out.println();
            }
        };

        AlarmTask noEventsAlarmTask = new AlarmTask() {
            public void run(Alarm alarm) {
                System.out.println(System.currentTimeMillis()+" - No Events Received!");
                System.out.println(alarm.getLap());
                System.out.println();
            }
        };

        AlarmTask avgLatencyTooHigh = new AlarmTask() {
            public void run(Alarm alarm) {
                System.out.println(System.currentTimeMillis()+" - Average Latency is too high!");
                System.out.println(alarm.getLap());
                System.out.println();
            }
        };

        AlarmTask maxLatencyTooHigh = new AlarmTask() {
            public void run(Alarm alarm) {
                System.out.println(System.currentTimeMillis()+" - Max Latency is too high!");
                System.out.println(alarm.getLap());
                System.out.println();
            }
        };

        Alarm rateAlarm = new Alarm(overRateLimitAlarmTask, 100, true);
        Alarm eventsAlarm = new Alarm(noEventsAlarmTask, 0, false);
        Alarm avgLatencyAlarm = new Alarm(avgLatencyTooHigh, 5.1, true);
        Alarm maxLatencyAlarm = new Alarm(maxLatencyTooHigh, 5.0, true);

        Lap hundredLap = monitor.addLap(hundredLapName, 100, true, 100);
        Lap secLap = monitor.addLap(secLapName, 1000, true, 100, new LapAction() {
            NumberFormat nf = NumberFormat.getInstance();
            public void execute(Lap lap) {
                System.out.println("One second has passed. Current lap rate = "+nf.format(lap.getRate()*1000)+" e/s");

            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
        PerformanceMonitor.setDateFormat(dateFormat);

        hundredLap.setTotalRateOnHistory(true);
        secLap.setTotalRateOnHistory(true);

        monitor.addRateAlarm(hundredLapName, rateAlarm);
        monitor.addEventAlarm(secLapName, eventsAlarm);
        monitor.addAverageLatencyAlarm(secLapName, avgLatencyAlarm);
        monitor.addMaxLatencyAlarm(secLapName, maxLatencyAlarm);

        monitor.start();

        monitor.update(10000, 50000);
        sleep(2500);
        monitor.update(9000, 50000);
        monitor.update(8000, 45000);
        sleep(500);
        monitor.update(7000, 38000);
        sleep(200);
        monitor.update(6000, 28000);
        sleep(100);
        monitor.update(5000, 24500);
        sleep(50);
        monitor.update(4000, 21000);
        sleep(5000);
        monitor.update(1, 5);
        monitor.update(1, 3);
        monitor.update(1, 3);
        monitor.update(1, 2);
        monitor.update(1, 7);
        monitor.update(1, 3);
        monitor.update(1, 9);
        monitor.update(1, 1);
        sleep(100);

        monitor.stop();

        System.out.println();
        monitor.printTotals(System.out);

        System.out.println();
        monitor.printLap(hundredLapName, System.out);

        System.out.println();
        monitor.printLap(secLapName, System.out);

        System.out.println();
        System.out.println(monitor.getLap(hundredLapName).getLapHistory().toString());

        System.out.println();
        System.out.println(monitor.getLap(secLapName).getLapHistory().toString());
    }

    private static final void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

}
