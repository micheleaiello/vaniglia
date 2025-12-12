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

package org.vaniglia.messagequeue.storage.impl.enhancedfsimpl;

import org.vaniglia.messagequeue.storage.MessageStorage;
import org.vaniglia.messagequeue.storage.MessageStorageType;
import org.vaniglia.messagequeue.storage.MessageStorageException;
import org.vaniglia.messagequeue.Message;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.Vector;

/**
 * File System based storage for Message Queues.
 * This Message Storage stores messages for the queue in the file system using plain files for the messages
 * and organizing the messages in subdirectories to keep track of their due date. The subdirectories organization
 * is based on the individual digits of the message due timestamp. The folder depth is 13.
 *
 * When a messages is popped out of the queue it is moved to an "handling" subdirectory until the processing of the
 * message is completed and than removed from the file system.
 */
public class MessageStorageEnhancedFileSystem extends MessageStorage {

    private static final Logger logger = Logger.getLogger(MessageStorageEnhancedFileSystem.class);

    private static final int numOfDigits = 13;
    private static final long divider = (long) Math.pow(10, (numOfDigits-1));

    private static final String dirExt = ".vmqfs";
    private static final String msgExt = ".vmqmsg";
    public static final String handlingSubdirName = "handling";
    private static final String defaultBasepath = "./VMQ";

    private String basepath;
    private File rootDir;
    private File handlingSubdir;

    private FilenameFilter messagesFilenameFilter = new MessagesFilenameFilter(msgExt);
    private FileFilter dirFilter = new FileFilter() {
        public boolean accept(File file) {
            return file.isDirectory();
        }
    };


    public MessageStorageEnhancedFileSystem(String name) {
        this(name, defaultBasepath);
    }


    public MessageStorageEnhancedFileSystem(String name, String basepath) {
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
        return MessageStorageType.EnhancedFileSystemType;
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

    public synchronized Message pop(long timestamp) {
        long ts = timestamp;
        long div = divider;

        File currentDir = rootDir;
        String currentPath = currentDir.getPath();
        boolean ltpath = false;
        long digit = 0;

        for (int level = 0; level < numOfDigits-3; level++) {
            if (ltpath) {
                digit = 9;
            }
            else {
                digit = ts/div;
                ts = ts - (digit*div);
                div = div/10;
            }

            for (int i = 0; i <= digit; i++) {
                String subPath = currentPath + "/" + i;
                File subDir = new File(subPath);
                if (subDir.exists()) {
                    currentDir = subDir;
                    currentPath = subPath;
                    if (i < digit) {
                        ltpath = true;
                    }
                    break;
                }
            }
        }

        boolean forcePrune = false;
        File[] msgs = currentDir.listFiles(messagesFilenameFilter);
        Arrays.sort(msgs);
        for (int k = 0; k < msgs.length; k++) {
            File crtMsg = msgs[k];
            Message msg = readMsg(crtMsg);
            if (msg != null) {
                if (msg.getTimestamp() <= timestamp) {
                    boolean moved = crtMsg.renameTo(new File(handlingSubdir.getPath()+"/"+ crtMsg.getName()));
                    if (forcePrune || (msgs.length == 1)) {
                        pruneDirectory(currentDir);
                    }
                    if (!moved) {
                        logger.error("Can't move message file "+crtMsg.getName());
                        return null;
                    }
                    return msg;
                }
            }
            else {
                logger.error("Unable to read file: "+crtMsg.getAbsolutePath());
                if ((crtMsg.isFile()) && (crtMsg.length() == 0)) {
                    logger.info("File '"+crtMsg.getName()+"' is unreadable and contains 0 bytes. The file will be deleted. File modification date is "+crtMsg.lastModified());
                    crtMsg.delete();
                    forcePrune = true;
                }
            }
        }

        if (forcePrune || (msgs.length == 0)) {
            pruneDirectory(currentDir);
        }

        return null;
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

    public void clear() {
        deleteDirectory(rootDir);
        initialize();
    }

    public String[] getMessageList() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Message[] getAllMessages() {
        Vector allMsgs = new Vector();

        addMessages(handlingSubdir, allMsgs);

        File currentDir = rootDir;
        String currentPath = currentDir.getPath();

        for (int level = 0; level < numOfDigits-3; level++) {

            for (int i = 0; i <= 9; i++) {
                String subPath = currentPath + "/" + i;
                File subDir = new File(subPath);
                if (subDir.exists()) {
                    currentDir = subDir;
                    currentPath = subPath;
                }
            }

        }

        return (Message[]) allMsgs.toArray(new Message[allMsgs.size()]);
    }

    private void addMessages(File directory, Vector allMsgs) {
        File[] msgs = directory.listFiles(messagesFilenameFilter);
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private String getDirectoryPath(final long timestamp) {
        StringBuffer path = new StringBuffer(rootDir.getPath().length()+22);

        long ts = timestamp;
        long div = divider;

        path.append(rootDir.getPath());
        path.append('/');

        for (int i = 0; i < numOfDigits-3; i++) {
            long digit = ts/div;
            path.append(digit);
            path.append('/');
            ts = ts - (digit*div);
            div = div/10;
        }

        return path.toString();
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
