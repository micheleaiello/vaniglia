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

import org.apache.commons.collections.FastHashMap;
import org.apache.log4j.Logger;
import org.vaniglia.messagequeue.storage.MessageStorage;
import org.vaniglia.messagequeue.storage.MessageStorageException;
import org.vaniglia.messagequeue.storage.MessageStorageType;
import org.vaniglia.messagequeue.storage.MessageStorageParameters;
import org.vaniglia.time.SystemDate;

import java.util.Timer;
import java.util.Vector;
import java.util.Set;
import java.util.Iterator;
import java.io.PrintStream;

/**
 * The MessageQueue is a queue of messages that are stored using a MessageStorega and are ordered by a timestamp.
 * The messages in the queue are processed by all the MessageListener subscribed to the queue and than removed.
 * Messages are processed only after their due date. The due date of a message is set to the message by calling the
 * push method of the MessageQueue.
 *
 */
public class MessageQueue {

    private static final Logger logger = Logger.getLogger(MessageQueue.class);

    private static final MessageStorageType defaultStorageType = MessageStorageType.MemoryType;
    private static final MessageStorageParameters defaultParams = MessageStorageParameters.createMemoryStorageParameters("Default Storage", 10000);

    private static final FastHashMap queues = new FastHashMap(5, 0.75f);

    private int processingIntervalMillis = 1000;

    private String name;
    private MessageStorage storage;

    private Timer timerThread;
    private MessageQueueProcessingTask processingTask;

    private Vector listeners;

    /**
     * Private constructor. To create a Message queue, the static method, getQueue must be used.
     *
     * @param name the name of the Message Queue.
     * @param storageType the Storage Type to use for the Message Queue.
     * @param params the parameters for the storage.
     */
    private MessageQueue(String name, MessageStorageType storageType, MessageStorageParameters params) {
        logger.info("Initializing queue "+name+" with Storage "+storageType.toString());
        this.name = name;

        this.storage = MessageStorage.getStorage(storageType, params);
        this.listeners = new Vector(1);
    }

    /**
     * This method is used to get a MessageQueue and set (at the same time) its underlaying storage system.
     * This method will raise and exception if the MessageQueue as been already created because in that case
     * the underlaying storage systema can't be changed.
     *
     * @param name the queue name
     * @param storageType the storage type for the queue
     * @param params the parameter for the storage
     *
     * @return the Message Queue
     *
     * @throws MessageQueueException if the Queue has been already created and the storage type is different from the
     * one used on the queue creation.
     */
    public static synchronized MessageQueue getQueue(String name, MessageStorageType storageType, MessageStorageParameters params)
            throws MessageQueueException
    {
        MessageQueue queue = (MessageQueue) queues.get(name);
        if (queue == null) {
            queue = new MessageQueue(name, storageType, params);
            queues.put(name, queue);
        }
        else {
            throw new MessageQueueException("Queue "+name+" has been already created.");
        }

        return queue;
    }

    /**
     * This method returns a Message Queue given its name.
     * The Queue will be created using the default storage type if has not been created yet.
     *
     * @param name the Message Queue's name
     *
     * @return the Message Queue with the given name
     */
    public static synchronized MessageQueue getQueue(String name) {
        MessageQueue queue = (MessageQueue) queues.get(name);
        if (queue == null) {
            queue = new MessageQueue(name, defaultStorageType, defaultParams);
            queues.put(name, queue);
        }

        return queue;
    }

    /**
     * This method wipes out a Message Queue from the system.
     * The Message Storage used by the Queue will also be deleted.
     *
     * @param name the name of the Message Queue to delete
     */
    public static synchronized void deleteQueue(String name) {
        MessageQueue queue = (MessageQueue) queues.get(name);
        if (queue == null) {
            logger.warn("Trying to delete a queue that doesn't exist. Queue name = "+name);
            return;
        }

        try {
            queue.stop();
        } catch (MessageQueueException e) {
        }

        queue.storage.clear();

        queues.remove(name);
    }

    /**
     * This method cleans up the system from all the existing Message Queues.
     */
    public static synchronized void removeAllQueues() {
        Set keySet = queues.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext()) {
            deleteQueue((String) it.next());
        }
    }

    /**
     * Returns the Queue name
     *
     * @return the queue name
     */
    public String getName() {
        return name;
    }

    /**
     * This method returns the queue processing interval (milliseconds).
     *
     * @return the queue processing interval (milliseconds)
     */
    public int getProcessingInterval() {
        return processingIntervalMillis;
    }

    /**
     * This method sets the queue processing interval (milliseconds).
     * The processing interval is the time elapse between processings of messages in the queue.
     * The default value is 1 second (1000 milliseconds).
     *
     * @param millis the queue processing interval.
     * @throws MessageQueueException if the queue has already been started.
     */
    public void setProcessingInterval(int millis) throws MessageQueueException {
        if (processingTask != null) {
            throw new MessageQueueException("Queue already started.");
        }
        this.processingIntervalMillis = millis;
    }

    /**
     * Starts the queue processing task.
     *
     * @throws MessageQueueException if the queue is already running.
     */
    public void start() throws MessageQueueException {
        start(processingIntervalMillis);
    }

    /**
     * Starts the queue processing task using the provided processing interval instead of the queue
     * own interval.
     *
     * @param interval the processing interval.
     *
     * @throws MessageQueueException if the queue is already running
     */
    public void start(long interval) throws MessageQueueException {
        if (timerThread == null) {
            logger.info("Starting queue "+name+" with processing interval = "+interval+"ms");
            timerThread = new Timer(true);
            processingTask = new MessageQueueProcessingTask(this);
            timerThread.scheduleAtFixedRate(processingTask, interval, interval);
        }
        else {
            throw new MessageQueueException("Queue "+name+" already running.");
        }
    }

    /**
     * Stops the queue
     *
     * @throws MessageQueueException if the queue is not running
     */
    public void stop() throws MessageQueueException {
        if (timerThread != null) {
            logger.info("Stopping queue "+name);
            timerThread.cancel();
            timerThread = null;
            processingTask = null;
        }
        else {
            throw new MessageQueueException("Queue "+name+" not running.");
        }
    }

    /**
     * Publish a message to the queue using the current time.
     *
     * @param msg the message to publish
     *
     * @throws MessageStorageException in case something goes wrong with the underlaying storage system.
     */
    public synchronized void publish(Message msg) throws MessageStorageException {
        publish(msg, SystemDate.getInstance().currentTimeMillis());
    }

    /**
     * Publish a message to the queue.
     *
     * @param msg the message to publish
     * @param timestamp the message due time.
     *
     * @throws MessageStorageException in case something goes wrong with the underlaying storage system.
     */
    public synchronized void publish(Message msg, long timestamp) throws MessageStorageException {
        if (logger.isDebugEnabled()) {
            logger.debug("Publishing message with ID: "+msg.getId()+" TS: "+timestamp);
        }
        storage.push(msg, timestamp);
    }

    /**
     * Subscribe a listener to the queue.
     *
     * @param listener the listener to subscribe
     *
     * @throws MessageQueueException if the listener is already subscribed
     */
    public synchronized void subscribe(MessageListener listener) throws MessageQueueException {
        for (int i = 0; i < listeners.size(); i++) {
            if (listener.equals(listeners.get(i))) {
                throw new MessageQueueException("Listener already subscribed");
            }
        }

        listeners.add(listener);
    }

    /**
     * Unsubscribes a listener from the queue
     *
     * @param listener the listener to unsubscribe
     *
     * @throws MessageQueueException if the listener is not subscribed
     */
    public synchronized void unsubscribe(MessageListener listener) throws MessageQueueException {
        boolean removed = listeners.remove(listener);
        if (!removed) {
            throw new MessageQueueException("Listener not subscribed");
        }
    }

    /**
     * Clears the queue. All messages in the queue will be lost.
     */
    public synchronized void clear() {
        if (storage != null) {
            storage.clear();
        }
    }

    /**
     * This is the method called by the Queue Processing Task.
     */
    void handle() {
        if (listeners.size() == 0) {
            logger.debug("Skipping messages processing. No listeners subscribed.");
            return;
        }

        Message msg = null;

        msg = storage.pop();
        while (msg != null) {
            logger.info("Begin Processing message "+msg.getId());
            for (int i = 0; i < listeners.size(); i++) {
                try {
                    ((MessageListener)listeners.get(i)).handle(msg);
                } catch (MessageQueueException e) {
                    logger.error("Exception handling message '"+msg.getId()+"'", e);
                }
            }
            logger.info("Finished Processing message "+msg.getId());

            try {
                storage.removeMessage(msg);
            } catch (MessageStorageException e) {
                logger.error("Exception removing message '"+msg.getId()+"'", e);
            }

            msg = storage.pop(SystemDate.getInstance().currentTimeMillis());
        }
    }

    /**
     * Returns the storage type used by the queue.
     *
     * @return the queue storage type.
     */
    public MessageStorageType getStorageType() {
        if (storage == null) {
            return null;
        }
        else {
            return storage.getType();
        }
    }

    /**
     * Returns the number of messages in the queue. This number does include also messages currently in handling.
     *
     * @return the number of messags in the queue.
     */
    public long size() {
        if (storage != null) {
            return storage.size();
        }
        else {
            return -1;
        }
    }

    /**
     * Returns a list of all messages in the queue. The list includes also messages currently in handling.
     *
     * @return a list of all the message's ids in the queue.
     */
    public String[] getMessageList() {
        if (storage != null) {
            return storage.getMessageList();
        }
        else {
            return new String[0];
        }
    }

    /**
     * Returns all messages in the queue.
     *
     * @return all messages in the queue.
     */
    public Message[] getAllMessages() {
        if (storage != null) {
            return storage.getAllMessages();
        }
        else {
            return new Message[0];
        }
    }

    /**
     * Prints out all messages in the queue to a given PrintStream.
     *
     * @param stream the stream used to print all messages.
     */
    public void printAllMessages(PrintStream stream) {
        if (storage != null) {
            stream.println("*** "+name+" ***");
            storage.printAllMessages(stream);
            stream.println();
        }
        else {
            stream.println("The Queue doesn't have a storage so there are no messages.");
        }
    }

}
