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

/**
 * This class is a container for parameters used on Message Storage creation.
 * The base (and abstract) class contains static methods to create and initialize the
 * different subtypes that are used for different Message Storage types.
 */
public abstract class MessageStorageParameters {

    public static class MemoryStorageParameters extends MessageStorageParameters {
        private long size;
        public MemoryStorageParameters(String name, long size) {
            super(name);
            this.size = size;
        }
        public long getSize() {
            return size;
        }
    }

    public static class FileSystemStorageParameters extends MessageStorageParameters {
        private String basedir;

        public FileSystemStorageParameters(String name) {
            super(name);
        }
        public FileSystemStorageParameters(String name, String basedir) {
            super(name);
            this.basedir = basedir;
        }
        public String getBasedir() {
            return basedir;
        }
    }

    public static class JDBCStorageParameters extends MessageStorageParameters {
        public JDBCStorageParameters(String name) {
            super(name);
        }
    }

    /**
     * Creates a Memory Storage Parameters given its name and size limit.
     *
     * @param name the storage name
     * @param size the storage size limit or -1 to avoid size limit checking.
     *
     * @return the newly created MemoryStorageParameters object
     */
    public static MessageStorageParameters createMemoryStorageParameters(String name, long size) {
        return new MemoryStorageParameters(name, size);
    }

    /**
     * Creates a FileSystem Storage Paramters given its name and basedir.
     *
     * @param name the storage name
     * @param basedir the storage basedir
     *
     * @return the newly create FileSystemStorageParamters object
     */
    public static MessageStorageParameters createFileSystemStorageParameters(String name, String basedir) {
        return new FileSystemStorageParameters(name, basedir);
    }

    /**
     * Creates a FileSystem Storage Paramters given its name.
     *
     * @param name the storage name
     *
     * @return the newly create FileSystemStorageParamters object
     */
    public static MessageStorageParameters createFileSystemStorageParameters(String name) {
        return new FileSystemStorageParameters(name);
    }

    public static MessageStorageParameters createJDBCStorageParameters(String name) {
        return new JDBCStorageParameters(name);
    }

    protected String name;

    protected MessageStorageParameters(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
