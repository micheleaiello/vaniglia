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

package org.vaniglia.socket.socketclient;

import org.apache.log4j.Logger;
import org.vaniglia.socket.CannotConnectException;

public abstract class AbstractSocketClient implements SocketClient {

    private static final Logger logger = Logger.getLogger(AbstractSocketClient.class);

    private String hostname;
    private int port;

    protected int clientSocketSoTimeout = 0;
    protected int numberOfRetries = 0;
    protected int retriesSleepingTime = 1000;

    private boolean connected;

    protected AbstractSocketClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        connected = false;
    }

    public synchronized final void connect()
            throws CannotConnectException {
        if (!connected) {
            doConnect();
            connected = true;
        }
    }

    protected abstract void doConnect() throws CannotConnectException;

    public synchronized final void disconnect() {
        if (connected) {
            doDisconnect();
            connected = false;
        }
    }

    protected abstract void doDisconnect();

    public synchronized final boolean isConnected() {
        return connected;
    }

    public synchronized final String getHostname() {
        return hostname;
    }

    public synchronized final int getPort() {
        return port;
    }

    public int getClientSocketSoTimeout() {
        return clientSocketSoTimeout;
    }

    public void setClientSocketSoTimeout(int clientSocketSoTimeout) {
        this.clientSocketSoTimeout = clientSocketSoTimeout;
    }

    public int getNumberOfRetries() {
        return numberOfRetries;
    }

    public void setNumberOfRetries(int numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }

    public int getRetriesSleepingTime() {
        return retriesSleepingTime;
    }

    public void setRetriesSleepingTime(int retriesSleepingTime) {
        this.retriesSleepingTime = retriesSleepingTime;
    }

}
