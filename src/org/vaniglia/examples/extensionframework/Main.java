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

package org.vaniglia.examples.extensionframework;

import org.vaniglia.extensionframework.ExtensionManager;
import org.vaniglia.extensionframework.ExtensionPointIdentifier;
import org.vaniglia.extensionframework.ExtensionManagerException;

public class Main {

    public static void main(String[] args) throws ExtensionManagerException {
        System.out.println("Starting up application...");

        ExtensionManager extensionManager = ExtensionManager.getInstance();

        extensionManager.loadExtensionPoints("testextension/extensionpoints.xml");
        extensionManager.loadExtensions("testextension");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        System.out.println();

        System.out.println("Calling via Extension Point Name");
        extensionManager.execute("SampleExtensionPoint");
        System.out.println();

        System.out.println("Calling via Extension Point Class");
        extensionManager.execute(SampleExtensionPoint.class);
        System.out.println();

        System.out.println("Calling via Extension Point Identifier");
        extensionManager.execute(new ExtensionPointIdentifier("20888405-e693-4bce-bcc4-1cdb6828645a"));
        System.out.println();

        System.out.println("Change the extension configuration... (1 minute before calling the extension again)");
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
        }

        System.out.println("Calling via Extension Point Name");
        extensionManager.execute("SampleExtensionPoint");
        System.out.println();

        extensionManager.shutdown();
    }

}
