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
package org.vaniglia.commandprotocol;

import java.net.Socket;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.IOException;

/**
 * This class is the client side component for the command protocol module.
 * It is responsibile for sending commands to a Command Server using the server address and port.
 */
public class CommandClient {

    private String hostname;
    private int port;

    public CommandClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void sendCommand(String command) throws IOException {
        sendCommand(command, null);
    }

    public void sendCommand(String command, String params) throws IOException {
        Socket socket = new Socket(hostname, port);

        Writer out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
//      in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

        out.write(command);
        out.write('\n');

        if (params != null) {
            out.write(params);
        }
        out.write('\n');
        out.flush();

        socket.close();
    }
}
