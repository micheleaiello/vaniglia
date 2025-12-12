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

import java.util.Random;

/**
 * This class generates unique identifiers.
 */
public class UIDGenerator {

    private static UIDGenerator _instance;

    private int gap = 100;

    private long lastUsedTimestamp = 0;
    private String currentTimestampHash;

    private Random random;
    private String internalSeed = "";

    private String[] stringifiedIndexes;
    private final int stringfiedBufferSize = 200000;

    private int index = 0;

    public synchronized static final UIDGenerator getInstance() {
        if (_instance == null) {
            _instance = new UIDGenerator();
        }
        return _instance;
    }

    private UIDGenerator() {
        random = new Random((System.currentTimeMillis()/cpuCycleTest()));
        stringifiedIndexes = new String[stringfiedBufferSize];
        for (int i = 0; i < stringfiedBufferSize; i++) {
            stringifiedIndexes[i] = Long.toHexString(i);
        }
        init(System.currentTimeMillis());
    }

    private void init(long current) {
        lastUsedTimestamp = current;
        currentTimestampHash = "-"+Long.toHexString(lastUsedTimestamp)+"-";
        internalSeed = Long.toHexString(random.nextLong());

        index = 0;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    public int getGap() {
        return gap;
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
            stringifiedIndex = Long.toHexString(index++);
        }

        return new StringBuffer(40).append(internalSeed).append(currentTimestampHash).append(stringifiedIndex).toString();
    }

    public String getUid(String seed) {
        long now = System.currentTimeMillis();
        if (now - lastUsedTimestamp > gap) {
            init(now);
        }

        String stringifiedIndex;
        if (index < stringfiedBufferSize) {
            stringifiedIndex = stringifiedIndexes[(index++)];
        }
        else {
            stringifiedIndex = Long.toHexString(index++);
        }

        return new StringBuffer(40).append(Long.toHexString(seed.hashCode())).append(currentTimestampHash).append(stringifiedIndex).toString();
    }

    private long cpuCycleTest()
    {
        long returnValue = 0;
        long startTime = System.currentTimeMillis();
        long rightNow = System.currentTimeMillis();

        while( (rightNow - startTime) < 10 &&
                returnValue < (Long.MAX_VALUE - 10) )
        {
            returnValue++;
            rightNow = System.currentTimeMillis();
        }

        return returnValue;
    }

}
