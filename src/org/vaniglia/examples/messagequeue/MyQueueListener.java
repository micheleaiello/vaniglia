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

import org.apache.log4j.Logger;
import org.vaniglia.messagequeue.Message;
import org.vaniglia.messagequeue.MessageListener;
import org.vaniglia.messagequeue.MessageQueueException;

public class MyQueueListener implements MessageListener {

    private static final Logger logger = Logger.getLogger(MyQueueListener.class);

    public void handle(Message msg) throws MessageQueueException {
        System.out.println(msg);
        logger.info(msg.toString());
    }
}
