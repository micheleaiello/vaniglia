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

package org.vaniglia.uid;

/**
 * This generator creates Unique identifiers that are formed by the current timestamp, plus a number if there are clashes.
 */
public class TimestampUIDGenerator {

    private static TimestampUIDGenerator _instance;
    private static final String padder = "000000000000000000000000000000000000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";

    private int gap = 100;

    private int timestampPaddingDigits = 0;
    private int indexPaddingDigits = 0;

    private long lastUsedTimestamp = 0;
    private String lastUsedTimestampStr;

    private int index = 0;

    private String[] stringifiedIndexes;
    private int stringfiedBufferSize = 1000;

    public static TimestampUIDGenerator getInstance() {
        if (_instance == null) {
            _instance = new TimestampUIDGenerator();
        }

        return _instance;
    }

    public TimestampUIDGenerator() {
        init(System.currentTimeMillis());
        initStringifiedIndexes();
    }

    private void init(long current) {
        lastUsedTimestamp = current;
        lastUsedTimestampStr = String.valueOf(lastUsedTimestamp);
        if (lastUsedTimestampStr.length() < timestampPaddingDigits) {
            lastUsedTimestampStr = padder.substring(0, timestampPaddingDigits - lastUsedTimestampStr.length()) + lastUsedTimestampStr;
        }

        index = 0;
    }

    private void initStringifiedIndexes() {
        stringifiedIndexes = new String[stringfiedBufferSize];
        for (int i = 0; i < stringfiedBufferSize; i++) {
            String indexStr = String.valueOf(i);
            if (indexStr.length() < indexPaddingDigits) {
                indexStr = padder.substring(0, indexPaddingDigits - indexStr.length()) + indexStr;
            }

            stringifiedIndexes[i] = indexStr;
        }
    }

    public void setGap(int gap) {
        this.gap = gap;
        init(System.currentTimeMillis());
    }

    public int getGap() {
        return gap;
    }

    public int getTimestampPaddingDigits() {
        return timestampPaddingDigits;
    }

    public void setTimestampPaddingDigits(int timestampPaddingDigits) {
        this.timestampPaddingDigits = timestampPaddingDigits;
        init(System.currentTimeMillis());
    }

    public int getIndexPaddingDigits() {
        return indexPaddingDigits;
    }

    public void setIndexPaddingDigits(int indexPaddingDigits) {
        this.indexPaddingDigits = indexPaddingDigits;
        init(System.currentTimeMillis());
        initStringifiedIndexes();
    }

    public int getStringfiedBufferSize() {
        return stringfiedBufferSize;
    }

    public void setStringfiedBufferSize(int stringfiedBufferSize) {
        this.stringfiedBufferSize = stringfiedBufferSize;
        initStringifiedIndexes();
    }

    public String getUid() {
        long now = System.currentTimeMillis();
        if (now - lastUsedTimestamp > gap) {
            init(now);
        }

        String stringifiedIndex;
        if (index < stringfiedBufferSize) {
            stringifiedIndex = stringifiedIndexes[(index++)];
        }
        else {
            String indexStr = String.valueOf(index++);
            if (indexStr.length() < indexPaddingDigits) {
                indexStr = padder.substring(0, indexPaddingDigits - indexStr.length()) + indexStr;
            }
            stringifiedIndex = indexStr;
        }

        return lastUsedTimestampStr+stringifiedIndex;
    }

}
