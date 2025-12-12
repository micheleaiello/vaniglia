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
package org.vaniglia.messagequeue;

import org.apache.log4j.Logger;
import org.vaniglia.time.SystemDate;

import java.util.TimerTask;

class MessageQueueProcessingTask extends TimerTask {

    private static final Logger logger = Logger.getLogger(MessageQueueProcessingTask.class);

    private MessageQueue queue;

    public MessageQueueProcessingTask(MessageQueue queue) {
        this.queue = queue;
    }

    public void run() {
        long start = SystemDate.getInstance().currentTimeMillis();

        if (logger.isDebugEnabled()) {
            logger.debug("Begin Processing queue "+queue.getName()+" TS: "+ start);
        }

        try {
            queue.handle();
        } catch (Throwable e) {
            logger.error("Exception while handling the queue...", e);
        }

        long end = SystemDate.getInstance().currentTimeMillis();

        if (logger.isDebugEnabled()) {
            logger.debug("Finished Processing queue "+queue.getName()+" TS: "+ end);
            logger.debug("Queue processing took "+(end-start)+"ms");
        }
    }
}
