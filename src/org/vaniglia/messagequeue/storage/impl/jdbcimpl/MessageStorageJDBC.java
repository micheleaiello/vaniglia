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

package org.vaniglia.messagequeue.storage.impl.jdbcimpl;

import org.vaniglia.messagequeue.storage.MessageStorage;
import org.vaniglia.messagequeue.storage.MessageStorageType;
import org.vaniglia.messagequeue.storage.MessageStorageException;
import org.vaniglia.messagequeue.Message;

import java.io.PrintStream;

/**
 * TBD
 */
public class MessageStorageJDBC extends MessageStorage {

    public MessageStorageJDBC(String name) {
        super(name);
    }

    public MessageStorageType getType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void push(Message msg, long timestamp) throws MessageStorageException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Message pop(long timestamp) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeMessage(Message msg) throws MessageStorageException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public long size() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clear() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getMessageList() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Message[] getAllMessages() {
        return new Message[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void printAllMessages(PrintStream stream) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
