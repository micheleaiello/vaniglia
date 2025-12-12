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

package org.vaniglia.examples.messagequeue;

import org.vaniglia.messagequeue.MessageQueue;
import org.vaniglia.messagequeue.storage.MessageStorageException;
import org.vaniglia.messagequeue.message.StringMessage;
import org.vaniglia.utils.SynchronizedSimpleDateFormat;
import org.vaniglia.time.SystemDate;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Random;

public class MyQueuePublisher implements Runnable {

    private static final Logger logger = Logger.getLogger(MyQueuePublisher.class);

    private static final SynchronizedSimpleDateFormat dateFormat = SynchronizedSimpleDateFormat.getFormat("HH:mm:ss");

    private MessageQueue queue;

    private boolean shutdown = false;

    public MyQueuePublisher(MessageQueue queue) {
        this.queue = queue;
    }

    public void run() {
        Random rnd = new Random(System.currentTimeMillis());
        while (!shutdown) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }

            long ts = SystemDate.getInstance().currentTimeMillis() + rnd.nextInt(20000);
            String tsStr = dateFormat.format(new Date(ts));
            StringMessage msg = new StringMessage("Current Time is: "+ tsStr);
            logger.info("Publishing message: "+msg.getId()+" TS: "+tsStr);

            try {
                queue.publish(msg, ts);
            } catch (MessageStorageException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            logger.info("Queue size is now: "+queue.size());
        }
    }

    public void shutdown () {
        shutdown = true;
    }
}
