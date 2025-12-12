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

package org.vaniglia.messagequeue.utils;

import org.vaniglia.messagequeue.Message;

import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class FileMessageViewer {

    public static void main(String[] args) {
        String filename = args[0];
        viewMessage(filename);
    }

    public static void viewMessage(String filename) {
        ObjectInputStream in = null;
        Message msg = null;
        try {
            in = new ObjectInputStream(new FileInputStream(filename));
            msg = (Message) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        if (msg != null) {
            System.out.println(msg);
            System.out.flush();
        }
    }
}
