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
package org.vaniglia.examples.commandprotocol;

import org.vaniglia.commandprotocol.CommandHandler;

public class MyCommandHandler implements CommandHandler {

    private static final String SHUTDOWN = "Shutdown";

    private boolean shutdown = false;

    public void commandReceived(String command, String parameters) {
        System.out.println("Received command "+command);
        if ((parameters != null) && (!parameters.equals(""))) {
            System.out.println("Parameters: "+parameters);
        }
        System.out.println();

        if (SHUTDOWN.equals(command)) {
            System.out.println("Shutting down...");
            shutdown = true;
        }
    }

    public boolean isShutdown() {
        return shutdown;
    }

}
