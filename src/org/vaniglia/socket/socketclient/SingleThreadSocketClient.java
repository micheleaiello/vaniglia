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

import org.vaniglia.socket.CannotConnectException;
import org.vaniglia.socket.NotConnectedException;
import org.vaniglia.socket.ConnectionException;
import org.vaniglia.socket.DelegateInstantiationException;
import org.apache.log4j.Logger;

import java.net.Socket;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class SingleThreadSocketClient extends AbstractSocketClient {

    private static final Logger logger = Logger.getLogger(SingleThreadSocketClient.class);

    private Socket socket;
    private OutputStreamWriter out;
    private BufferedReader in;

    private ClientDelegate delegate;

    public SingleThreadSocketClient(String hostname, int port, Class clientDelegateClass)
            throws DelegateInstantiationException
    {
        super(hostname, port);

        logger.debug("Checking Delegate Class.");
          if ( !ClientDelegate.class.isAssignableFrom(clientDelegateClass)) {
              logger.error("The supplied delegate class does't implements the ClientDelegate interface. Raising an exception");
              throw new DelegateInstantiationException("Delegate class doesn't implements ClientDelegate. Client Delegate Class = " + clientDelegateClass.getName());
            }
        logger.debug("OK. Delegate Class implements ClientDelegate.");

        try {
            delegate = (ClientDelegate)clientDelegateClass.newInstance();
        } catch (InstantiationException e) {
            throw new DelegateInstantiationException("InstantiationException - Empty constructor not found. Client Delegate Class = " + clientDelegateClass.getName());
        } catch (IllegalAccessException e) {
            throw new DelegateInstantiationException("IllegalAccessException - Empty constructor is not accessible. Client Delegate Class = " + clientDelegateClass.getName());
        }
    }

    protected void doConnect() throws CannotConnectException {
        String hostname = getHostname();
        int port = getPort();
        logger.info("Connecting to the server on " + hostname + ":" + port);

        try {
            this.socket = new Socket(hostname, port);

            if (clientSocketSoTimeout > 0) {
                this.socket.setSoTimeout(clientSocketSoTimeout);
            }
            this.socket.setTcpNoDelay(true);

            this.out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        }
        catch (IOException e) {
            logger.error("IOException.", e);
            logger.error("Disconnecting and raising an Exception.");
            disconnect();
            throw new CannotConnectException();
        }
    }

    protected void doDisconnect() {
        if (this.out != null) {
            try {
                this.out.close();
            }
            catch (IOException e) {
                logger.error("Exception while closing the output stream", e);
            }
        }

        if (this.in != null) {
            try {
                this.in.close();
            }
            catch (IOException e) {
                logger.error("Exception while closing the input stream", e);
            }
        }

        if (this.socket != null) {
            try {
                this.socket.close();
            }
            catch (IOException e) {
                logger.error("Exception while closing the socket", e);
            }
        }

        this.out = null;
        this.in = null;
        this.socket = null;
    }

    public String invoke(String request)
            throws NotConnectedException, ConnectionException
    {
        if (!super.isConnected()) throw new NotConnectedException();

        String retValue = null;

        try {
            send(request);
            retValue = receive();
        } catch (IOException e) {
            logger.warn("IOException.");
            int retries = getNumberOfRetries();
            int retriesSleepingTime = getRetriesSleepingTime();
            if (retries <= 0) {
                logger.error("Connection failed.");
                throw new ConnectionException("IOException: "+e.getMessage());
            }
            while (retries > 0) {
                logger.info("Trying to reconnect...["+retries+" tries left]");
                retries--;
                try {
                    disconnect();
                    connect();
                    send(request);
                    retValue = receive();
                    break;
                } catch (IOException e1) {
                    if (retries <= 0) {
                        logger.error("Connection failed after "+getNumberOfRetries()+" retries.");
                        throw new ConnectionException("IOException:"+e1.getMessage());
                    }
                } catch (CannotConnectException e1) {
                    if (retries <= 0) {
                        logger.error("Connection failed after "+getNumberOfRetries()+" retries.");
                        throw new ConnectionException(e.getMessage());
                    }
                }
                try {
                    logger.info("Sleeping... ["+retriesSleepingTime+" ms]");
                    Thread.sleep(retriesSleepingTime);
                } catch (InterruptedException e1) {
                }
            }
        }

        return retValue;
    }

    private void send(String request) throws IOException {
        logger.debug("Sending request...");
        delegate.writeRequest(request, out);
        out.flush();
        logger.debug("Sending end...");
    }

    private String receive() throws IOException {
        logger.debug("Receiving value...");
        String value = delegate.readResult(in);
        logger.debug("Received value...");
        return value;
    }

}
