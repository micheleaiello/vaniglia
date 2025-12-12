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
package org.vaniglia.examples.polling;

import org.vaniglia.polling.DirectoryPoller;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        File inputDirectory = new File("./input/");
        File tmpDirectory = new File("./input/working/");
        File finalDirectory = new File("./input/final/");

        DirectoryPoller poller = new DirectoryPoller(inputDirectory);
        poller.setPollInterval(5000);
        poller.addEventListener(new DummyListener());
        poller.setAutoMove(true);
        poller.setAutoMoveDirectory(inputDirectory, tmpDirectory);
        poller.setFinalDirectory(inputDirectory, finalDirectory);
        poller.setVerbose(true);
        poller.setAppendTimestampToFinalNames(true);
        poller.setCreateExceptionDescriptionFile(true);
        poller.setSortFiles(true);
        poller.setModificationInterval(100);

        poller.start();
    }

}
