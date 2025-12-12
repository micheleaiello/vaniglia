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

package org.vaniglia.messagequeue.storage.impl.fsimpl;

import org.vaniglia.messagequeue.Message;
import org.vaniglia.messagequeue.storage.MessageStorageException;
import org.vaniglia.messagequeue.storage.MessageStorage;
import org.vaniglia.messagequeue.storage.MessageStorageType;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.Vector;

/**
 * File System based storage for Message Queues.
 * This Message Storage stores messages for the queue in the file system using plain files for the messages
 * and organizing the messages in subdirectories to keep track of their due date.
 *
 * When a messages is popped out of the queue it is moved to an "handling" subdirectory until the processing of the
 * message is completed and than removed from the file system.
 */
public class MessageStorageFileSystem extends MessageStorage {

    private static final Logger logger = Logger.getLogger(MessageStorageFileSystem.class);

    private static final String dirExt = ".vmqfs";
    private static final String msgExt = ".vmqmsg";
    public static final String handlingSubdirName = "handling";
    private static final String defaultBasepath = "./VMQ";

    private String basepath;
    private File rootDir;
    private File handlingSubdir;

    private SubdirectoriesFilter filter = new SubdirectoriesFilter();
    private FileFilter dirFilter = new OnlyDirectoriesFilter();
    private FilenameFilter messagesFilenameFilter = new MessagesFilenameFilter(msgExt);

    public MessageStorageFileSystem(String name) {
        this(name, defaultBasepath);
    }

    public MessageStorageFileSystem(String name, String basepath) {
        super(name);
        this.basepath = basepath;

        initialize();
    }

    private void initialize() {
        rootDir = new File(this.basepath+"/"+this.name+dirExt);
        handlingSubdir = new File(this.basepath+"/"+this.name+dirExt+"/"+handlingSubdirName);

        if (rootDir.exists()) {
            if (handlingSubdir.exists()) {
                File[] recoveryMsgs = handlingSubdir.listFiles(messagesFilenameFilter);
                if (recoveryMsgs.length > 0) {
                    logger.info("There are messages left in the handling directory.");
                    logger.info("Beginning messages recovery...");
                }
                int numOfRecoveredMsgs = 0;
                int errors = 0;
                for (int i = 0; i < recoveryMsgs.length; i++) {
                    Message msg = null;
                    msg = readMsg(recoveryMsgs[i]);

                    if (msg != null) {
                        try {
                            push(msg, msg.getTimestamp());
                            recoveryMsgs[i].delete();
                            numOfRecoveredMsgs++;
                        } catch (MessageStorageException e) {
                            logger.debug(e);
                            errors++;
                        }
                    }
                    else {
                        recoveryMsgs[i].delete();
                        errors++;
                    }
                }
                if (recoveryMsgs.length > 0) {
                    if (errors > 0) {
                        logger.info(numOfRecoveredMsgs+" messages recovered and "+errors+" not recovered because of errors.");
                    }
                    else {
                        logger.info(numOfRecoveredMsgs+" messages recovered.");
                    }
                    logger.info("Finished messages recovery...");
                }
            }
            else {
                handlingSubdir.mkdirs();
            }
        }
        else {
            rootDir.mkdirs();
            handlingSubdir.mkdirs();
        }
    }

    public MessageStorageType getType() {
        return MessageStorageType.FileSystemType;
    }

    public synchronized void push(Message msg, long timestamp) throws MessageStorageException {
        // IN PROGRESS exception if the is a problem with the file system (file system full, etc).
        String id = msg.getId();
        msg.setTimestamp(timestamp);

        String dirPath = getDirectoryPath(timestamp);
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dirPath+"/"+id+msgExt);
        if (file.exists()) {
            throw new MessageStorageException("Message already existent!");
        }
        else {
            writeMsg(msg, file);
        }
        // IN PROGRESS file with difference extensions and than move to the final name to avoid
        // the message from being selected while is still opened.
    }

    private void writeMsg(Message msg, File file) {
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(msg);
        } catch (IOException e) {
            logger.error("IOException while writing msg: "+msg.getId()+" - file: "+file.getAbsolutePath(), e);
            logger.error(msg.toString());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private Message readMsg(File file) {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(file));
            Message msg = (Message) in.readObject();
            return msg;
        } catch (IOException e) {
            logger.error("IOException while reading file: "+file.getAbsolutePath(), e);
        } catch (ClassNotFoundException e) {
            logger.error("ClassNotFoundException while reading file: "+file.getAbsolutePath(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        return null;
    }

    public synchronized Message pop(long timestamp) {
        filter.setTimestamp(timestamp);
        filter.setLevel(SubdirectoriesFilter.FIRST_LEVEL);
        File[] firstLeveldirs = rootDir.listFiles(filter);

//        Arrays.sort(firstLeveldirs, numberFilenameComparator);
        Arrays.sort(firstLeveldirs);

        filter.setLevel(SubdirectoriesFilter.SECOND_LEVEL);
        for (int i = 0; i < firstLeveldirs.length; i++) {
            File[] secondLevelDirs = firstLeveldirs[i].listFiles(filter);
//            Arrays.sort(secondLevelDirs, numberFilenameComparator);
            Arrays.sort(secondLevelDirs);
            for (int j = 0; j < secondLevelDirs.length; j++) {
                File[] msgs = secondLevelDirs[j].listFiles(messagesFilenameFilter);
//                Arrays.sort(msgs, numberFilenameComparator);
                Arrays.sort(msgs);
                for (int k = 0; k < msgs.length; k++) {
                    File crtMsg = msgs[k];
                    Message msg = readMsg(crtMsg);
                    if (msg != null) {
                        if (msg.getTimestamp() <= timestamp) {
                            boolean moved = crtMsg.renameTo(new File(handlingSubdir.getPath()+"/"+ crtMsg.getName()));
                            if (!moved) {
                                logger.error("Can't move message file "+crtMsg.getName());
                                return null;
                            }
                            if (msgs.length == 1) {
                                pruneDirectory(secondLevelDirs[j]);
                            }
                            return msg;
                        }
                    }
                    else {
                        logger.error("Unable to read file: "+crtMsg.getAbsolutePath());
                    }
                }
                if (msgs.length == 0) {
                    pruneDirectory(secondLevelDirs[j]);
                }                
            }
        }

        return null;
    }

    private void pruneDirectory(File dir) {
        if ((dir == null) || (!dir.exists() || (!dir.isDirectory()))) {
            return;
        }

        if (dir.equals(this.rootDir)) {
            return;
        }

        if (dir.list().length == 0) {
            File parentDir = dir.getParentFile();
            dir.delete();
            pruneDirectory(parentDir);
        }
    }

    public synchronized void removeMessage(Message msg) throws MessageStorageException {
        if (msg == null) {
            throw new MessageStorageException("Null message");
        }
        String id = msg.getId();
        File msgFile = new File(handlingSubdir.getPath()+"/"+id+msgExt);
        if (msgFile.exists()) {
            msgFile.delete();
        }
        else {
            throw new MessageStorageException(msgFile.getName()+" doesn't exist.");
        }
    }

    public long size() {
        return fileCount(rootDir);
    }

    private long fileCount(File root) {
        String[] msgsFileNames = root.list(messagesFilenameFilter);
        int numOfMsgs = msgsFileNames.length;

        File[] subdirs = root.listFiles(dirFilter);
        for (int i = 0; i < subdirs.length; i++) {
            numOfMsgs += fileCount(subdirs[i]);
        }

        return numOfMsgs;
    }

    public void clear() {
        deleteDirectory(rootDir);
        initialize();
    }

    public String[] getMessageList() {
        Vector msgsList = new Vector();

        addMessageItems(handlingSubdir, msgsList);

        File[] firstLeveldirs = rootDir.listFiles();

//        Arrays.sort(firstLeveldirs, numberFilenameComparator);
        Arrays.sort(firstLeveldirs);

        for (int i = 0; i < firstLeveldirs.length; i++) {
            if (!firstLeveldirs[i].equals(handlingSubdir)) {
                File[] secondLevelDirs = firstLeveldirs[i].listFiles();
//                Arrays.sort(secondLevelDirs, numberFilenameComparator);
                Arrays.sort(secondLevelDirs);
                for (int j = 0; j < secondLevelDirs.length; j++) {
                    addMessageItems(secondLevelDirs[j], msgsList);
                }
            }
        }

        return (String[]) msgsList.toArray(new String[msgsList.size()]);
    }

    private void addMessageItems(File directory, Vector msgsList) {
        File[] msgs = directory.listFiles(messagesFilenameFilter);
//        Arrays.sort(msgs, numberFilenameComparator);
        Arrays.sort(msgs);
        for (int k = 0; k < msgs.length; k++) {
            File crtMsg = msgs[k];
            String name = crtMsg.getName();
            int index = name.lastIndexOf('.');
            if (index > 0) {
                msgsList.add(name.substring(0, index));
            }
            else {
                msgsList.add(name);
            }
        }
    }

    public Message[] getAllMessages() {
        Vector allMsgs = new Vector();

        addMessages(handlingSubdir, allMsgs);
        File[] firstLeveldirs = rootDir.listFiles();

//        Arrays.sort(firstLeveldirs, numberFilenameComparator);
        Arrays.sort(firstLeveldirs);

        for (int i = 0; i < firstLeveldirs.length; i++) {
            if (!firstLeveldirs[i].equals(handlingSubdir)) {
                File[] secondLevelDirs = firstLeveldirs[i].listFiles();
//                Arrays.sort(secondLevelDirs, numberFilenameComparator);
                Arrays.sort(secondLevelDirs);
                for (int j = 0; j < secondLevelDirs.length; j++) {
                    addMessages(secondLevelDirs[j], allMsgs);
                }
            }
        }

        return (Message[]) allMsgs.toArray(new Message[allMsgs.size()]);
    }

    private void addMessages(File directory, Vector allMsgs) {
        File[] msgs = directory.listFiles(messagesFilenameFilter);
//        Arrays.sort(msgs, numberFilenameComparator);
        Arrays.sort(msgs);
        for (int k = 0; k < msgs.length; k++) {
            File msgFile = msgs[k];
            Message msg = readMsg(msgFile);
            if (msg != null) {
                allMsgs.add(msg);
            }
        }
    }

    public void printAllMessages(PrintStream stream) {
        stream.println("Messages in handling: ");

        printMessages(handlingSubdir, stream);

        File[] firstLeveldirs = rootDir.listFiles();
//        Arrays.sort(firstLeveldirs, numberFilenameComparator);
        Arrays.sort(firstLeveldirs);

        stream.println("");
        stream.println("Messages in the queue: ");

        for (int i = 0; i < firstLeveldirs.length; i++) {
            if (!firstLeveldirs[i].equals(handlingSubdir)) {
                File[] secondLevelDirs = firstLeveldirs[i].listFiles();
//                Arrays.sort(secondLevelDirs, numberFilenameComparator);
                Arrays.sort(secondLevelDirs);
                for (int j = 0; j < secondLevelDirs.length; j++) {
                    printMessages(secondLevelDirs[j], stream);
                }
            }
        }
    }

    private void printMessages(File directory, PrintStream stream) {
        File[] msgs = directory.listFiles(messagesFilenameFilter);
//        Arrays.sort(msgs, numberFilenameComparator);
        Arrays.sort(msgs);
        for (int k = 0; k < msgs.length; k++) {
            File msgFile = msgs[k];
            Message msg = readMsg(msgFile);
            if (msg != null) {
                stream.print('\t');
                stream.print(msg.toString());
                stream.println();
            }
        }
    }

    private String getDirectoryPath(long timestamp) {
        long ts2 = timestamp/60000;
        long ts1 = timestamp/3600000;
        return rootDir.getPath()+"/"+ts1+"/"+ts2;
    }

    private static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
}
