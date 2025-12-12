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

import java.net.Socket;
import java.io.*;

class SocketReceiver implements Runnable {

    private static final Logger logger = Logger.getLogger(SocketReceiver.class);
    private static final boolean yield = true;

    private Socket clientSocket;
    private boolean ready;
    private long requestsHandled;
    private ServerDelegate delegate;

    private boolean shutdown = false;

    private SocketReceiver(ServerDelegate delegate) {
        logger.debug("Creating a SocketReceiver");

        logger.debug("Yield = "+String.valueOf(yield));

        ready = true;
        requestsHandled = 0;
        this.delegate = delegate;
        logger.debug("Delegate Class = "+delegate.getClass().getName());
    }

    static SocketReceiver createSocketReceiver(ServerDelegate delegate) {
        SocketReceiver receiver = new SocketReceiver(delegate);
        Thread t = new Thread(receiver);
        int priority = t.getThreadGroup().getMaxPriority();
        logger.debug("Setting SocketReceiver priority = "+priority+".");
        t.setPriority(priority);
        logger.debug("Starting the SocketReceiver.");
        t.start();
        return receiver ;
    }

    public void run() {
        logger.debug("Called run."+Thread.currentThread());
        while (true) {

            synchronized(this) {
                while (ready == true) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            if (shutdown) {
                logger.debug("Shutdown = true. Breaking the loop.");
                break;
            }

            logger.debug(Thread.currentThread()+"Going!");
            logger.info("Handling Requests from: "+clientSocket.getInetAddress()+":"+clientSocket.getPort());

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);

                delegate.init();

                while (true) {
                    if (shutdown) {
                        logger.debug("Shutdown = true. Breaking the communication.");
                        break;
                    }

                    String request = delegate.readRequest(in);
                    if (request == null) {
                        break;
                    }

                    String result = delegate.handleRequest(request);

                    delegate.writeResult(result, out);
                    out.flush();

                    requestsHandled++;
                    if (yield) {
                        Thread.yield();
                    }

                }
            } catch (java.net.SocketTimeoutException e) {
                logger.error("SocketTimeoutException");
                logger.debug("SocketTimeoutException", e);
                if (shutdown) {
                    logger.debug("Shutdown = true. Breaking the loop.");
                    break;
                }
            } catch (java.net.SocketException e) {
                logger.error("SocketException");
                logger.debug("SocketException", e);
                if (shutdown) {
                    logger.debug("Shutdown = true. Breaking the loop.");
                    break;
                }
            } catch (IOException e) {
                logger.error("IOException");
                logger.debug("IOException", e);
                if (shutdown) {
                    logger.debug("Shutdown = true. Breaking the loop.");
                    break;
                }
            } finally {
                logger.info("Closed connection from: "+clientSocket.getInetAddress()+":"+clientSocket.getPort());
                try {
                    clientSocket.close();
                } catch (IOException e) {
                }

                clientSocket = null;
            }

            delegate.close();
            ready = true;
            logger.debug(Thread.currentThread()+"Finished!");

            synchronized(this) {
                notify();
            }

            if (shutdown) {
                logger.debug("Shutdown = true. Breaking the loop.");
                break;
            }
        }

        logger.debug("Exit from run.");
    }

    public synchronized void handle(Socket socket) {
        logger.info("Accepted Request from: "+socket.getInetAddress()+":"+socket.getPort());
        while (ready == false) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        clientSocket = socket;
        ready = false;
        notify();
    }

    public boolean isReady() {
        return ready;
    }

    public long getNumberOfRequestsHandled() {
        return requestsHandled;
    }

    public synchronized void shutdown() {
        logger.debug("Shutdown called.");
        shutdown = true;
        ready = false;
        notify();
    }


}
