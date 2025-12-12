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
package org.vaniglia.polling;


/**
 * Clients implementing this interface and registering receive method calls
 * whenever an event occur.
 */
public interface FileEventListener {

    /**
     * If the automove mode is active, is invoked for each automatic move
     * operation executed by the poller.
     *
     * @param evt the file moved event
     */
    public void fileMoved(FileMovedEvent evt);

    /**
     * Invoked for each of the files found, if setSendSingleFileEvent()
     * has been invoked before starting the poller thread.
     *
     * @param evt the file set found event
     */
    public void fileFound(FileFoundEvent evt) throws Exception;

    /**
     * Invoked when an automove operation fails since the given file
     * cannot be deleted
     *
     * @param evt the exception event
     */
    public void exceptionDeletingTargetFile(ExceptionEvent evt);

    /**
     * Invoked when an automove operation fails since the given file
     * cannot be moved to the given destination
     *
     * @param evt the exception event
     */
    public void exceptionMovingFile(ExceptionEvent evt);

    /**
     * Invoked when the poller is shutting down.
     */
    public void shutdown();

}