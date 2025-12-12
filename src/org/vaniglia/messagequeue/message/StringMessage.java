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

package org.vaniglia.messagequeue.message;

import org.vaniglia.messagequeue.Message;

public class StringMessage extends Message {

    private static final long serialVersionUID = 1;

    private String body;

    public StringMessage(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public String toString() {
        return new StringBuffer().append("Message [ID: ").append(id).append(" - TS: ").append(timestamp).append("] (").append(body).append(")").toString();
    }
}
