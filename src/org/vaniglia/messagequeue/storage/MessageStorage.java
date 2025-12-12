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

package org.vaniglia.messagequeue.storage;

import org.vaniglia.messagequeue.Message;
import org.vaniglia.time.SystemDate;

import java.io.PrintStream;

/**
 * This is the base class for Message Storages.
 */
public abstract class MessageStorage {

    protected String name;

    /**
     * This method is used to create a Message Storage given its name and storage type.
     *
     * @param storageType the storage type of the Message Storage
     * @param params the parameters for the storage
     *
     * @return a newly created Message Storage with the given name and of the given storage type.
     */
    public static MessageStorage getStorage(MessageStorageType storageType, MessageStorageParameters params) {
        return storageType.getStorage(params);
    }

    /**
     * Class constructor.
     *
     * @param name the message storage name
     */
    protected MessageStorage(String name) {
        this.name = name;
    }

    /**
     * Retruns the name of the Message Storage.
     *
     * @return the name of the Message Storage.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the Message Storage.
     *
     * @return the type of the Message Storage.
     */
    public abstract MessageStorageType getType();

    /**
     * Push a new message in the storage.
     * Shall the message be already in the storage, an exception will be raised.
     *
     * @param msg the message to push in.
     * @param timestamp the message due timestamp.
     *
     * @throws MessageStorageException if the Message is already in the storage.
     */
    public abstract void push(Message msg, long timestamp) throws MessageStorageException;

    /**
     * Pops out a message from the storage with a due date earlier than the provided timestamp.
     * The returned message is not removed from the storage but is moved into a special storage area
     * from where can be deleted using the removeMessage method.
     *
     * @param timestamp the timestamp
     *
     * @return the first message in the storage with msg.timestamp <= timestamp
     */
    public abstract Message pop(long timestamp);

    /**
     * Pops out a message from the storage with a due date earlier than now.
     * The returned message is not removed from the storage but is moved into a special storage area
     * from where can be deleted using the removeMessage method.
     *
     * @return the first message in the storage with msg.timestamp <= now
     */
    public Message pop() {
        return pop(SystemDate.getInstance().currentTimeMillis());
    }

    /**
     * Removes a message from the storage special stocking area of messages previously returned using
     * one of the pop methods.
     *
     * @param msg the message to remove.
     *
     * @throws MessageStorageException if the message is not in the storage special stocking area.
     */
    public abstract void removeMessage(Message msg) throws MessageStorageException;

    /**
     * Returns the number of messages in the storage.
     *
     * @return the number fo messages in the storage.
     */
    public abstract long size();

    /**
     * Removes all the messages from the storage, including messages in the special stocking area.
     */
    public abstract void clear();

    /**
     * Returns a list of all message's ids in the storage.
     *
     * @return a list of all message's ids in the storage.
     */
    public abstract String[] getMessageList();

    /**
     * Retruns all the messages in the storage including messages in the special area.
     * Returned messages are not moved in the storage special stocking area and so cannot be removed from the storage
     * before popping them out first.
     *
     * @return all the messages in the storage.
     */
    public abstract Message[] getAllMessages();

    public abstract void printAllMessages(PrintStream stream);

}
