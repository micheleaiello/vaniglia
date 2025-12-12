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

package org.vaniglia.socket.socketserver;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

public interface ServerDelegate {

    /**
     * This method is called before any request. It is used to initialize the ServerDelegate.
     */
    void init();

    /**
     * This method is used by the SocketServer to read the request form the input socket.
     *
     * @param in the input channel
     * @return the request read
     */
    String readRequest(BufferedReader in) throws IOException;

    /**
     * This method is used to handle the request.
     *
     * @param request the input request
     * @return the result
     */
    String handleRequest(String request);

    /**
     * This method is used to write the result to the output socket.
     *
     * @param result the result to write
     * @param out the output channel
     */
    void writeResult(String result, PrintWriter out);

    /**
     * This method is called after all the request are executed and before removing the ServerDelegate.
     * It is used to perform clean up operations
     */
    void close();
}
