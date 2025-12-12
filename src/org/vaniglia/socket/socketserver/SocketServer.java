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

import org.apache.log4j.Logger;
import org.vaniglia.socket.DelegateInstantiationException;
import org.vaniglia.socket.CannotConnectException;

import java.net.ServerSocket;
import java.net.SocketException;
import java.net.Socket;
import java.util.Vector;
import java.io.IOException;

public class SocketServer implements Runnable {

    private static final Logger logger = Logger.getLogger(SocketServer.class);

    static private final int sleepTime = 1000;
    static private final int numOfRetry = 10;

    private int portNumber;
    private ServerSocket serverSocket;
    private Vector receiversPool;

    private boolean shutdown = false;

    private int serverSocketSoTimeout = 0;
    private int clientSocketSoTimeout = 0;

    public SocketServer(int port, int numOfThreads, Class serverDelegateClass)
            throws DelegateInstantiationException
    {
        logger.debug("Creating a SocketServer.");
        logger.debug("Port number = "+port);
        logger.debug("Number of threads = "+numOfThreads);
        logger.debug("Server delegate = "+serverDelegateClass.getName());

        logger.debug("Checking Delegate Class.");
          if ( ! ServerDelegate.class.isAssignableFrom(serverDelegateClass)) {
              logger.error("The supplied delegate class does't implements the ServerDelegate interface. Raising an exception");
              throw new DelegateInstantiationException("Delegate class doesn't implements ServerDelegate. Server Delegate Class = " + serverDelegateClass.getName());
            }
        logger.debug("OK. Delegate Class implements ServerDelegate.");

        portNumber = port;
        receiversPool = new Vector();

        SocketReceiver[] receivers = new SocketReceiver[numOfThreads];
        for (int i = 0; i < numOfThreads; i++) {
            ServerDelegate delegate = null;
            try {
                delegate = (ServerDelegate)serverDelegateClass.newInstance();
            } catch (InstantiationException e) {
                throw new DelegateInstantiationException("InstantiationException - Empty constructor not found. Server Delegate Class = " + serverDelegateClass.getName());
            } catch (IllegalAccessException e) {
                throw new DelegateInstantiationException("IllegalAccessException - Empty constructor is not accessible. Server Delegate Class = " + serverDelegateClass.getName());
            }
            SocketReceiver receiver = SocketReceiver.createSocketReceiver(delegate);
            receiversPool.addElement(receiver);
            receivers[i] = receiver;
        }
    }

    public void bind() throws CannotConnectException
    {
        unbind();
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            logger.error("Can't open server socket on port "+portNumber, e);
            throw new CannotConnectException();
        }
        logger.info("SocketServer ready on port "+portNumber);

        if (serverSocketSoTimeout > 0) {
            logger.debug("Setting Server Socket SoTimeout to "+serverSocketSoTimeout+"ms");
            try {
                serverSocket.setSoTimeout(serverSocketSoTimeout);
            } catch (SocketException e) {
                logger.error("Exception while setting SoTimeout for the server socket", e);
            }
        }

        System.out.println("SocketServer ready on port "+portNumber);
    }

    public void unbind() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("Exception while closing server socket", e);
            }
            serverSocket = null;
        }
    }

    public void shutdown() {
        logger.debug("Shutdown called.");
        shutdown = true;

        logger.debug("Shutting down all the receivers.");
        int numOfRec = receiversPool.size();
        for (int i = 0; i < numOfRec; i++) {
            SocketReceiver rec = (SocketReceiver) receiversPool.get(i);
            rec.shutdown();
        }
    }

    public void run() {
        int receiverIndex = 0;
        int receiversPoolSize = receiversPool.size();
        while (true) {
            try {
                Socket clientSocket = null;
                clientSocket = serverSocket.accept();

                if (shutdown) {
                    logger.debug("Shutdown = true. Breaking the loop.");
                    break;
                }

                logger.debug("Request accepted!");

                logger.debug("Dispatching Request.");
                boolean handling = false;
                int initialIndex = receiverIndex;
                int loopNum = 0;

                while (!handling) {
                    SocketReceiver receiver = (SocketReceiver)receiversPool.get(receiverIndex);
                    receiverIndex = (receiverIndex + 1) % receiversPoolSize;
                    if (receiver.isReady()) {
                        handling = true;
                        if (clientSocketSoTimeout > 0) {
                            logger.debug("Setting client socket SoTimeout to "+clientSocketSoTimeout+"ms");
                            try {
                                clientSocket.setSoTimeout(clientSocketSoTimeout);
                            } catch (SocketException e) {
                                logger.error("Exception while setting SoTimeout for client socket", e);
                            }
                        }
                        clientSocket.setTcpNoDelay(true);
                        receiver.handle(clientSocket);
                    }
                    else {
                        Thread.yield();
                        if (receiverIndex == initialIndex) {
                            logger.debug("Can't dispatch the request. All Receivers are busy.");
                            loopNum++;
                            if (loopNum >= numOfRetry) {
                                logger.error("Total number of tries exceted. Closing the connection.");
                                handling = true;
                                clientSocket.close();
                                System.out.println("Connection refused (too many clients) from: "+clientSocket.getInetAddress()+":"+clientSocket.getPort());
                                logger.error("Connection refused (too many clients) from: "+clientSocket.getInetAddress()+":"+clientSocket.getPort());
                            }
                            else {
                                try {
                                    Thread.sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    logger.debug("Interrupted.", e);
                                    if (shutdown) {
                                        logger.debug("Shutdown = true. Breaking the loop.");
                                        break;
                                    }
                                }
                            }
                        }
                    }

                }
            } catch (java.net.SocketTimeoutException e) {
                logger.debug("SocketTimeoutException");
                if (shutdown) {
                    logger.debug("Shutdown = true. Breaking the loop.");
                    break;
                }
            } catch (java.net.SocketException e) {
                logger.warn("SocketException");
                logger.debug("SocketException", e);
                if (shutdown) {
                    logger.debug("Shutdown = true. Breaking the loop.");
                    break;
                }
            } catch (IOException e) {
                logger.warn("IOException");
                logger.debug("IOException", e);
                if (shutdown) {
                    logger.debug("Shutdown = true. Breaking the loop.");
                    break;
                }
            }
            if (shutdown) {
                logger.debug("Shutdown = true. Breaking the loop.");
                break;
            }

        }

        unbind();
        logger.debug("Exit from run.");
    }

    public int getServerSocketSoTimeout() {
        return serverSocketSoTimeout;
    }

    public void setServerSocketSoTimeout(int serverSocketSoTimeout) {
        this.serverSocketSoTimeout = serverSocketSoTimeout;
    }

    public int getClientSocketSoTimeout() {
        return clientSocketSoTimeout;
    }

    public void setClientSocketSoTimeout(int clientSocketSoTimeout) {
        this.clientSocketSoTimeout = clientSocketSoTimeout;
    }

}
