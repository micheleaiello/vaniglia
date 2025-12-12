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

import org.vaniglia.messagequeue.storage.impl.fsimpl.MessageStorageFileSystem;
import org.vaniglia.messagequeue.storage.impl.jdbcimpl.MessageStorageJDBC;
import org.vaniglia.messagequeue.storage.impl.memimpl.MessageStorageMemory;
import org.vaniglia.messagequeue.storage.impl.enhancedfsimpl.MessageStorageEnhancedFileSystem;

/**
 * Enumeration class for all the Message Storage Types available.
 *
 * - MemoryType. Memory based storage. Messages are kept in memory and if the application terminates or crashes than
 *               all messages will be lost.
 *
 * - FileSystemType. File system based storage. Messages are stored on file system file using a directory structure.
 *                   No messages will be lost in case of application termination or crash. Messages that where been
 *                   in handling when the application terminated will be recovered and reprocessed at a future startup.
 *
 * - EnhancedFileSystemType. File system based storage. This Storage type is similar to the FileSystem storage type but
 *                           adds some improvements to reduce push and pop delays. The main drawback of this storage
 *                           over the plain FileSystemType is a very complex directory structure.
 *
 * - JDBCType. Database based storage. Messages are stored on a JDBC compliant database. This Storage System is yet
 *             to be implemented
 */
public abstract class MessageStorageType {

    public static final MessageStorageType MemoryType =
            new MessageStorageType("MemoryType") {
                public MessageStorage getStorage(MessageStorageParameters params) {
                    String name = params.getName();
                    if (params instanceof MessageStorageParameters.MemoryStorageParameters) {
                        MessageStorageParameters.MemoryStorageParameters memStorageParams = (MessageStorageParameters.MemoryStorageParameters) params;
                        long size = memStorageParams.getSize();
                        return new MessageStorageMemory(name, size);
                    }
                    else {
                        return new MessageStorageMemory(name);
                    }
                }
            };

    public static final MessageStorageType FileSystemType =
            new MessageStorageType("FileSystemType") {
                public MessageStorage getStorage(MessageStorageParameters params) {
                    String name = params.getName();
                    if (params instanceof MessageStorageParameters.FileSystemStorageParameters) {
                        MessageStorageParameters.FileSystemStorageParameters fsParams = (MessageStorageParameters.FileSystemStorageParameters) params;
                        String basePath = fsParams.getBasedir();
                        return new MessageStorageFileSystem(name, basePath);
                    }
                    else {
                        return new MessageStorageFileSystem(name);
                    }
                }
            };

    public static final MessageStorageType EnhancedFileSystemType =
            new MessageStorageType("EnhancedFileSystemType") {
                public MessageStorage getStorage(MessageStorageParameters params) {
                    String name = params.getName();
                    if (params instanceof MessageStorageParameters.FileSystemStorageParameters) {
                        MessageStorageParameters.FileSystemStorageParameters fsParams = (MessageStorageParameters.FileSystemStorageParameters) params;
                        String basePath = fsParams.getBasedir();
                        return new MessageStorageEnhancedFileSystem(name, basePath);
                    }
                    else {
                        return new MessageStorageEnhancedFileSystem(name);
                    }
                }
            };

    public static final MessageStorageType JDBCType =
            new MessageStorageType("JDBCType") {
                public MessageStorage getStorage(MessageStorageParameters params) {
                    String name = params.getName();
                    return new MessageStorageJDBC(name);
                }
            };

    private final String name;

    private MessageStorageType(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    /**
     * Returns a storage with the given name.
     *
     * @param params the storage creation parameters
     *
     * @return a newly create Message Storage
     */
    public abstract MessageStorage getStorage(MessageStorageParameters params);

}
