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

import org.vaniglia.socket.socketclient.ClientDelegate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

public class ExampleClientDelegate implements ClientDelegate {

    private static final String clientVersion = "001";
    private static final String padder = "000000000000000000000000000000000000000000000000000000000000000";

    private char[] header = new char[11];

    public void writeRequest(String request, OutputStreamWriter out) throws IOException {
        String messageLengh = String.valueOf(request.length());
        String bodyLength = padder.substring(0, 7-messageLengh.length())+messageLengh;

        out.write(clientVersion);
        out.write(bodyLength);
        out.write('\t');
        out.write(request);
    }

    public String readResult(BufferedReader in) throws IOException {
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

        if (!clientVersion.equals(version)) {
            return "Wrong version. Client version = "+clientVersion+" Result version = "+version;
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

}
