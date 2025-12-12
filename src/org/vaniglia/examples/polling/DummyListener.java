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

import org.vaniglia.polling.ExceptionEvent;
import org.vaniglia.polling.FileEventListener;
import org.vaniglia.polling.FileFoundEvent;
import org.vaniglia.polling.FileMovedEvent;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DummyListener implements FileEventListener {

    public void fileMoved(FileMovedEvent evt) {
        System.out.println("fileMoved: " + evt.getPath().getName());
    }

    public void fileFound(FileFoundEvent evt) throws Exception {
        System.out.println("fileFound: " + evt.getFile().getName());
        System.out.println("Printing first 10 lines...");
        String str = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(evt.getFile()));
            for (int i = 0; i < 10; i++) {
                str = reader.readLine();
                if (str == null) {
                    break;
                }
                System.out.println(str);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }

        if (str == null) {
            System.out.println("EOF");
        }
        else {
            System.out.println("...");
        }
//        throw new java.lang.NullPointerException("Test Exception");
    }

    public void exceptionDeletingTargetFile(ExceptionEvent evt) {
        System.out.println("exceptionDeletingTargetFile");
    }

    public void exceptionMovingFile(ExceptionEvent evt) {
        System.out.println("exceptionMovingFile");
    }

    public void shutdown() {
        System.out.println("shutdown");
    }
}
