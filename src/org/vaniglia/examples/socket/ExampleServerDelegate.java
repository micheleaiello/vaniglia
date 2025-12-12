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

package org.vaniglia.examples.socket;

import org.vaniglia.socket.socketserver.ServerDelegate;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Arrays;

public class ExampleServerDelegate implements ServerDelegate {

    private static final String serverVersion = "001";
    private static final String padder = "000000000000000000000000000000000000000000000000000000000000000";

    private char[] header;

    public void init() {
        header = new char[11];
        Arrays.fill(header, '0');
    }

    public String readRequest(BufferedReader in) throws IOException {
        Arrays.fill(header, '0');
        int n;
        do {
            n = in.read(header, 0, header.length);
        } while (n == 0);
        if (n < header.length) {
            return null;
        }

        String version = new String(header, 0, 3);
        String sizeStr = new String(header, 3, 7);

        if (!serverVersion.equals(version)) {
            return "Wrong version. Server version = "+serverVersion+" Request version = "+version;
        }
        int size = Integer.parseInt(sizeStr);

        char[] body = new char[size];
        do {
            n = in.read(body, 0, body.length);
        } while (n == 0);
        if (n < body.length) {
            return null;
        }

        return new String(body);
    }

    public String handleRequest(String request) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return request+request;
    }

    public void writeResult(String result, PrintWriter out) {
        String messageLengh = String.valueOf(result.length());
        String bodyLength = padder.substring(0, 7-messageLengh.length())+messageLengh;

        out.write(serverVersion);
        out.write(bodyLength);
        out.write('\t');
        out.write(result);
    }

    public void close() {
    }

}
