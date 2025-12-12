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

import org.apache.log4j.Logger;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

/**
 * This class is the server side component for the command protocol module.
 * Using the Command Protocol you can send and receive simple remote commands.
 * The Command Server is the class responsible for handling the server side and so it receives
 * commands from a client and dispatch it to a given handler.
 *
 * If a socket can't be opened on the given port, than the Thread will end immediatly.
 */
public class CommandServer extends Thread {

    private static Logger logger = Logger.getLogger(CommandServer.class);

    private int port;
    private CommandHandler handler;
    private int soTimeout;

    /**
     * Constructor for the class.
     *
     * @param port the port used to receive commands.
     * @param handler the command handler.
     */
    public CommandServer(int port, CommandHandler handler) {
        this(port, handler, 0);
    }

    /**
     * Constructor for the class.
     *
     * @param port the port used to receive commands.
     * @param handler the command handler.
     * @param soTimeout the socket timeout in milliseconds. 0 means no timeout. if set to a number greater than 0,
     * than the server socket will timeout and the CommandServer has a chance to check if the application has been
     * shutted down using the isShutdown method of the CommandHandler.
     */
    public CommandServer(int port, CommandHandler handler, int soTimeout) {
        super();
        this.port = port;
        this.handler = handler;
        this.soTimeout = soTimeout;

        setDaemon(true);
    }

    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(soTimeout);
        }
        catch (IOException e) {
            logger.fatal("Can't listen on port " + port);
            return;
        }

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
//                PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);

                String command = in.readLine();
                String params = null;
                try {
                    params = in.readLine();
                } catch (IOException e) {
                }

                handler.commandReceived(command, params);

                clientSocket.close();
                if (handler.isShutdown()) {
                    break;
                }
            }
            catch(java.net.SocketTimeoutException e) {
            }
            catch (IOException e) {
                logger.error(e);
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.error(e);
        }
    }

}
