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

import org.vaniglia.socket.CannotConnectException;
import org.vaniglia.socket.ConnectionException;
import org.vaniglia.socket.DelegateInstantiationException;
import org.vaniglia.socket.NotConnectedException;
import org.vaniglia.socket.socketclient.ConnectionPoolSocketClient;
import org.vaniglia.socket.socketclient.SocketClient;

public class ClientPool {

    public static void main(String[] args)
            throws DelegateInstantiationException, CannotConnectException, ConnectionException,
            NotConnectedException
    {
        SocketClient client = new ConnectionPoolSocketClient("localhost", 8888, 4, ExampleClientDelegate.class);
        client.setClientSocketSoTimeout(0);
        client.setNumberOfRetries(0);
        client.setRetriesSleepingTime(0);

        client.connect();

        String result = client.invoke("Pippo");
        System.out.println("Result: "+result);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }

        result = client.invoke("Topolino");
        System.out.println("Result: "+result);

        client.disconnect();
    }

}
