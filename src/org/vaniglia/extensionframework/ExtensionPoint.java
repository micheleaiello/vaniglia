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

package org.vaniglia.extensionframework;

import java.util.Vector;

public abstract class ExtensionPoint {

    private String name;
    private Vector registeredExtensions;

    protected ExtensionPoint() {
        registeredExtensions = new Vector();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    void addExtension(Extension ext) {
        registeredExtensions.add(ext);
    }

    protected Vector getAllExtension() {
        return registeredExtensions;
    }

    public abstract Class getExtensionInterface();

    public abstract ExtensionPointIdentifier getId();

    public abstract void init(ExtensionPointParameters parameters) throws InitializationException;

    public abstract void execute(Extension extension);

    public abstract void shutdown();

}
