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

import org.vaniglia.extensionframework.ExtensionPoint;
import org.vaniglia.extensionframework.Extension;
import org.vaniglia.extensionframework.ExtensionPointParameters;
import org.vaniglia.extensionframework.ExtensionPointIdentifier;

public class SampleExtensionPoint extends ExtensionPoint {

    public SampleExtensionPoint() {
    }

    public Class getExtensionInterface() {
        return SampleExtensionInterface.class;
    }

    public ExtensionPointIdentifier getId() {
        return new ExtensionPointIdentifier("20888405-e693-4bce-bcc4-1cdb6828645a");
    }

    public void init(ExtensionPointParameters parameters) {
        System.out.println("Initializing Sample Extension Point...");
    }

    public void execute(Extension extension) {
        SampleExtensionInterface ext = (SampleExtensionInterface) extension;
        ext.print();
    }

    public void shutdown() {
        System.out.println("Shutting down Sample Extension Point...");
    }
}
