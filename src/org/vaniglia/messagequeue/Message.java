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

package org.vaniglia.messagequeue;

import org.vaniglia.uid.TimestampUIDGenerator;

import java.io.Serializable;

public abstract class Message implements Comparable, Serializable {

    protected String id;
    protected long timestamp;

    static {
        TimestampUIDGenerator.getInstance().setIndexPaddingDigits(5);
    }

    protected Message() {
        id = TimestampUIDGenerator.getInstance().getUid();
        timestamp = 0;
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int compareTo(Object o) {
        if (this == o) return 0;
        final Message that = (Message)o;

        return ((timestamp<that.timestamp)?-1:((timestamp==that.timestamp)?0:1));
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (timestamp != message.timestamp) return false;
        if (id != null ? !id.equals(message.id) : message.id != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}
