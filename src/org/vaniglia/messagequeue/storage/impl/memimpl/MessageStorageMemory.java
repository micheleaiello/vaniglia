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

package org.vaniglia.messagequeue.storage.impl.memimpl;

import org.vaniglia.messagequeue.Message;
import org.vaniglia.messagequeue.storage.MessageStorage;
import org.vaniglia.messagequeue.storage.MessageStorageException;
import org.vaniglia.messagequeue.storage.MessageStorageType;

import java.util.*;
import java.io.PrintStream;

/**
 * Memory based Message Storage
 * All messages are kept in a list in memory. Messages in handling (popped messages) are moved to an hashmap
 * waiting for processing to be completed and than will be deleted.
 */
public class MessageStorageMemory extends MessageStorage {

    private LinkedList messagesList;
    private HashMap handling;
    private long size;

    public MessageStorageMemory(String name) {
        super(name);
        size = -1;
        messagesList = new LinkedList();
        handling = new HashMap();
    }


    public MessageStorageMemory(String name, long size) {
        super(name);
        this.size = size;
        messagesList = new LinkedList();
        handling = new HashMap();
    }

    public MessageStorageType getType() {
        return MessageStorageType.MemoryType;
    }

    public synchronized void push(Message msg, long timestamp) throws MessageStorageException {
        if ((size > 0) && (size() >=  size)) {
            throw new MessageStorageException("Size limit reached.");
        }

        msg.setTimestamp(timestamp);
        ListIterator it = messagesList.listIterator();

        while (it.hasNext()) {
            Message currentMsg = (Message) it.next();
            if (msg.compareTo(currentMsg) <= 0) {
                it.previous();
                it.add(msg);
                return;
            }
        }

        it.add(msg);
    }

    public synchronized Message pop(long timestamp) {
        ListIterator it = messagesList.listIterator();

        while (it.hasNext()) {
            Message currentMsg = (Message) it.next();
            if (currentMsg.getTimestamp() <= timestamp) {
                it.remove();
                handling.put(currentMsg.getId(), currentMsg);
                return currentMsg;
            }
        }

        return null;
    }

    public synchronized void removeMessage(Message msg) throws MessageStorageException {
        if (msg == null) {
            throw new MessageStorageException("Null message");
        }

        if (handling.remove(msg.getId()) == null) {
            throw new MessageStorageException("Message with ID: "+msg.getId()+" doesn't exist in the storage.");
        }
    }

    public long size() {
        return messagesList.size() + handling.size();
    }

    public synchronized void clear() {
        messagesList.clear();
        handling.clear();
    }

    public synchronized String[] getMessageList() {
        String[] msgs = new String[messagesList.size()+handling.size()];
        int i = 0;

        Set keySet = handling.keySet();
        Iterator itSet = keySet.iterator();
        while (itSet.hasNext()) {
            msgs[i++] = (String)itSet.next();
        }

        ListIterator it = messagesList.listIterator();
        while (it.hasNext()) {
            Message currentMsg = (Message) it.next();
            msgs[i++] = currentMsg.getId();
        }

        return msgs;
    }

    public synchronized Message[] getAllMessages() {
        Message[] msgs = new Message[messagesList.size()+handling.size()];
        int i = 0;

        Set entries = handling.entrySet();
        Iterator itSet = entries.iterator();
        while (itSet.hasNext()) {
            msgs[i++] = (Message) ((Map.Entry)itSet.next()).getValue();
        }

        ListIterator it = messagesList.listIterator();
        while (it.hasNext()) {
            msgs[i++] = (Message) it.next();
        }

        return msgs;

    }

    public synchronized void printAllMessages(PrintStream stream) {
        stream.println("Messages in handling: ");
        Set entries = handling.entrySet();
        Iterator itSet = entries.iterator();
        while (itSet.hasNext()) {
            Message msg = (Message) ((Map.Entry)itSet.next()).getValue();
            stream.print('\t');
            stream.print(msg.toString());
            stream.println();
        }

        stream.println("");
        stream.println("Messages in the queue: ");

        ListIterator it = messagesList.listIterator();
        while (it.hasNext()) {
            Message msg  = (Message) it.next();
            stream.print('\t');
            stream.print(msg.toString());
            stream.println();
        }
    }
}
