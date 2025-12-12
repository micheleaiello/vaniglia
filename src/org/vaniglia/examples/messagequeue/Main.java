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
import org.vaniglia.messagequeue.MessageQueueException;
import org.vaniglia.messagequeue.storage.MessageStorageType;
import org.vaniglia.messagequeue.storage.MessageStorageParameters;
import org.apache.log4j.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) throws MessageQueueException {
        MessageQueue queue = MessageQueue.getQueue("MyQueue", MessageStorageType.MemoryType,
                MessageStorageParameters.createMemoryStorageParameters("MyStorage", 1000));

        queue.start();

        queue.subscribe(new MyQueueListener());

        MyQueuePublisher myQueuePublisher = new MyQueuePublisher(queue);
        Thread publishingThread = new Thread(myQueuePublisher);
        publishingThread.setDaemon(true);
        logger.info("Starting the publisher.");
        publishingThread.start();

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
        }

        logger.info("Stopping the publisher.");
        myQueuePublisher.shutdown();

        try {
            publishingThread.join();
        } catch (InterruptedException e) {
        }

        try {
            Thread.sleep(22000);
        } catch (InterruptedException e) {
        }

        queue.stop();

        if (queue.size() != 0) {
            System.err.println("Queue should be empty but still contains "+queue.size()+" messages.");
        }
    }

}
