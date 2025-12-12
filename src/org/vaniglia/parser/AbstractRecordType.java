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
package org.vaniglia.parser;

import org.vaniglia.pool.vsop.MTObjectsPool;
import org.apache.log4j.Logger;

public abstract class AbstractRecordType implements RecordType {

    private static final Logger logger = Logger.getLogger(AbstractRecordType.class);

    private MTObjectsPool pool;

    private boolean[] constantFields;
    private RecordProcessor recordProcessor;

    protected AbstractRecordType(boolean[] constantFields, RecordProcessor recordProcessor) {
        pool = new MTObjectsPool();

        this.constantFields = constantFields;
        this.recordProcessor= recordProcessor;
    }

    public Record createRecord() {
        Record record = (Record) pool.getObject();
        if (record != null) {
            record.clear();
        }
        else {
            record = _createRecord();
        }
        return record;
    }

    public void releaseRecord(Record record) {
        try {
            pool.releaseObject(record);
        } catch (Exception e) {
        }
    }

    public void postProcessRecord(Record record) {
        if (recordProcessor != null) {
            recordProcessor.processRecord(record);
        }
    }

    public void initConstantFields(Record record) {
        if (constantFields == null) return;

        for (int i = 0; i < constantFields.length; i++) {
            if (constantFields[i]) {
                try {
                    this.setFieldValue(record, i, null);
                } catch (InvalidFieldException e) {
                    logger.error(e);
                } catch (InvalidFieldValueException e) {
                    logger.error(e);
                }
            }
        }
    }

    public int getNextNonConstantFieldIndex(int index) {
        int startIndex = ((index>=0)?(index+1):0);

        if (constantFields == null) {
            return startIndex;
        }

        if (startIndex < constantFields.length) {
            for (int i = startIndex; i < constantFields.length; i++) {
                if (!constantFields[i]) {
                    return i;
                }
            }
            return constantFields.length;
        }
        else {
            return startIndex;
        }
    }

    public abstract FieldType fieldTypeAt(int index);

    protected abstract Record _createRecord();

}
