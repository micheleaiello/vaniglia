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
package org.vaniglia.performancemonitor;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

import java.text.SimpleDateFormat;
import java.util.Iterator;

/**
 * This class contains the history for a Lap.
 */
public class LapHistory {

    /**
     * History depth.
     */
    private int depth;

    /**
     * Records storing buffer
     */
    private CircularFifoBuffer buffer;

    private SimpleDateFormat dateFormat;

    /**
     * Default constructor.
     *
     * @param depth the number of record to be maintained.
     */
    public LapHistory(int depth) {
        this.depth = depth;
        buffer = new CircularFifoBuffer(depth);
    }

    /**
     * Adds a new record to the history log.
     *
     * @param record the record to add.
     */
    public void addRecord(LapHistoryRecord record) {
        buffer.add(record);
    }

    /**
     * This method is used to get all the history records.
     * The size of the returned array will be less or equal to the history depth.
     *
     * @return an array of all the history records.
     */
    public LapHistoryRecord[] getRecords() {
        LapHistoryRecord[] records = new LapHistoryRecord[0];
        records = (LapHistoryRecord[]) buffer.toArray(records);

        return records;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        Iterator it = buffer.iterator();
        while (it.hasNext()) {
            LapHistoryRecord record = (LapHistoryRecord) it.next();
            buff.append(record.toString());
            buff.append('\n');
        }
        return buff.toString();
    }

}
