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

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;

/**
 * This is a parser for Token Speratated Values (TSV) file.
 * The format of the records in the file is defined via the RecordType.
 */
public class Parser {

    private static Logger logger = Logger.getLogger(Parser.class);

    public static final char DEFAULT_TOKEN = ',';
    public static final char DEFAULT_COMMENT = '#';

    private static final String recoveryFileExtention = ".vpr";

    /**
     * Default token = comma. The token is the fields separator.
     */
    private char token;

    /**
     * Default comment = hash. The comment starting char for comment lines.
     */
    private char comment;

    /**
     * Number of record between two update on the positions file.
     * If set to 0 or less, the positions file will not be used.
     */
    private int recovery;

    private boolean flush = false;

    /**
     * The record type for the file to parse.
     */
    private RecordType recordType;

    /**
     * Currently open file name.
     */
    private String filename;

    /**
     * Input file readed.
     */
    private BufferedReader reader = null;

    /**
     * Positions file. This file is used to recover in case of crash.
     */
    private DataOutputStream recoveryFileStream = null;

    /**
     * Current position for the open file.
     */
    private long currentPosition;

    private Thread shutdownHook = null;

    private static class ShutdownRecoveryNotifier implements Runnable {

        private Parser parser;

        public ShutdownRecoveryNotifier(Parser parser) {
            this.parser = parser;
        }

        public void run() {
            try {
                parser.updatePositionFile(true);
            } catch (IOException e) {
            }
        }
    }

    public Parser(RecordType recordType) {
        this(recordType, DEFAULT_TOKEN, DEFAULT_COMMENT, 0);
    }

    public Parser(RecordType recordType, char token) {
        this(recordType, token, DEFAULT_COMMENT, 0);
    }

    public Parser(RecordType recordType, char token, int recovery) {
        this(recordType, token, DEFAULT_COMMENT, recovery);
    }

    public Parser(RecordType recordType, char token, char comment, int recovery) {
        this.token = token;
        this.comment = comment;
        this.recordType = recordType;
        this.recovery = recovery;
    }

    public int getRecovery() {
        return recovery;
    }

    public void setRecovery(int recovery) throws ParserException {
        if (reader != null) {
            throw new ParserException("File already open, can't change recovery now.");
        }

        this.recovery = recovery;
    }

    public boolean getFlush() {
        return flush;
    }

    public void setFlush(boolean flush) {
        this.flush = flush;
    }

    public boolean deleteRecovery(String filename) {
        File recoveryFile = new File(filename+recoveryFileExtention);
        return recoveryFile.delete();
    }

    private boolean isComment(String buff) {
        boolean isComment = (buff != null) && (buff.length() > 0) && (buff.trim().charAt(0) == comment);
        if ((logger.isDebugEnabled()) && (isComment)) {
            logger.debug("The line '"+buff+"' is a comment");
        }
        return isComment;
    }

    /**
     * This method opens a file for parsing.
     *
     * @param filename the input file name
     *
     * @return the current file position. Usually is 0, but can be different in case of a recovery.
     *
     * @throws ParserException if the input file can't be found or can't be opened.
     */
    public long open(String filename) throws ParserException {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }

        if (recoveryFileStream != null) {
            try {
                recoveryFileStream.close();
            } catch (IOException e) {
            }

//            File posFile = new File(this.filename+recoveryFileExtention);
//            posFile.delete();
        }

        if (shutdownHook != null) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }

        this.filename = filename;
        this.reader = null;
        this.recoveryFileStream = null;
        this.shutdownHook = null;

        try {
            reader = new BufferedReader(new FileReader(this.filename));
        } catch (FileNotFoundException e) {
            throw new ParserException("File '"+filename+"' not found.");
        }

        currentPosition = 0;

        File recoveryFile = new File(this.filename+recoveryFileExtention);
        if (recoveryFile.exists()) {
            logger.info("Found a recovery file "+this.filename+recoveryFileExtention);
            DataInputStream positionsFileReader;
            try {
                positionsFileReader = new DataInputStream(new FileInputStream(this.filename+recoveryFileExtention));
            } catch (IOException e) {
                throw new ParserException("Unable to open recovery file '"+this.filename+recoveryFileExtention +"' for reading", e);
            }

            try {
                for (;;)
                    currentPosition = positionsFileReader.readLong();
            } catch (IOException e) {
            }

            logger.info("Recovering from position: "+currentPosition);

            try {
                positionsFileReader.close();
            } catch (IOException e) {
            }

            for (int i = 0; i < currentPosition; i++) {
                try {
                    reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (recovery > 0) {
            try {
                recoveryFileStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.filename+recoveryFileExtention)));
            } catch (IOException e) {
                throw new ParserException("Unable to open recovery file '"+this.filename+recoveryFileExtention +"' for writing", e);
            }

            try {
                updatePositionFile(flush);
            } catch (IOException e) {
            }

            shutdownHook = new Thread(new ShutdownRecoveryNotifier(this));
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
        else {
            recoveryFile.delete();
        }

        return currentPosition;
    }

    public boolean hasMoreRecords() throws ParserException {
        if (reader == null) {
            throw new ParserException("No file open");
        }

        boolean retValue = false;
        try {
            retValue = reader.ready();
        } catch (IOException e) {
            retValue = false;
        }

        return retValue;
    }

    public Record getNextRecord() throws ParserException, InvalidFieldValueException {
        if (reader == null) {
            throw new ParserException("No file opened");
        }

        Record record = null;
        String buff = null;
        try {
            do {
                buff = reader.readLine();
                if (buff == null) return null;
            } while ((buff.length() == 0) || isComment(buff));

            int nextFieldBegin = 0;
            record = recordType.createRecord();
            record.setSourceRecord(buff);
            int fieldIndex = recordType.getNextNonConstantFieldIndex(-1);
            for (int i = 0; i < buff.length(); i++) {
                if (buff.charAt(i) == token) {
                    recordType.setFieldValue(record, fieldIndex, buff.substring(nextFieldBegin, i));
                    fieldIndex = recordType.getNextNonConstantFieldIndex(fieldIndex);
                    nextFieldBegin = i + 1;
                }
            }
            recordType.setFieldValue(record, fieldIndex, buff.substring(nextFieldBegin, buff.length()));
            recordType.initConstantFields(record);

            recordType.postProcessRecord(record);
            currentPosition++;
            if (recoveryFileStream != null) {
                if ((currentPosition % recovery) == 0) {
                    updatePositionFile(flush);
                }
            }
        } catch (InvalidFieldException e) {
            e.printStackTrace();
        } catch (InvalidFieldValueException e) {
            e.setSource(buff);
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        } catch(NullPointerException e) {
            e.printStackTrace();
        }

        return record;
    }

    public Record parseString(String value) throws InvalidFieldValueException {
        Record record = null;
        if (value.length() == 0) return null;
        if (isComment(value)) return null;

        try {
            int nextFieldBegin = 0;
            record = recordType.createRecord();
            record.setSourceRecord(value);
            int fieldIndex = 0;
            for (int i = 0; i < value.length(); i++) {
                if (value.charAt(i) == token) {
                    recordType.setFieldValue(record, fieldIndex++, value.substring(nextFieldBegin, i));
                    nextFieldBegin = i + 1;
                }
            }
            recordType.setFieldValue(record, fieldIndex++, value.substring(nextFieldBegin, value.length()));
        } catch (InvalidFieldException e) {
            e.printStackTrace();
        } catch (InvalidFieldValueException e) {
            throw e;
        } catch(NullPointerException e) {
            e.printStackTrace();
        }

        return record;
    }

    public Record parseString(String value, boolean skipConsecutiveTokens) throws InvalidFieldValueException {
        Record record = null;
        if (value.length() == 0) return null;
        if (isComment(value)) return null;

        try {
            int nextFieldBegin = 0;
            record = recordType.createRecord();
            record.setSourceRecord(value);
            int fieldIndex = 0;
            int i = 0;
            if (skipConsecutiveTokens) {
                while ((value.charAt(i) == token) && (i < value.length())) {
                    i++;
                }
                if (i > 0) {
                    i--;
                }
            }
            nextFieldBegin = i;
            while (i < value.length()) {
                if (value.charAt(i) == token) {
                    recordType.setFieldValue(record, fieldIndex++, value.substring(nextFieldBegin, i));
                    if (skipConsecutiveTokens) {
                        while ((value.charAt(i) == token) && (i < value.length())) {
                            i++;
                        }
                    }
                    else {
                        i++;
                    }
                    nextFieldBegin = i;
                }
                else {
                    i++;
                }
            }
            recordType.setFieldValue(record, fieldIndex++, value.substring(nextFieldBegin, value.length()));
        } catch (InvalidFieldException e) {
            e.printStackTrace();
        } catch (InvalidFieldValueException e) {
            throw e;
        } catch(NullPointerException e) {
            e.printStackTrace();
        }

        return record;
    }


    private synchronized void updatePositionFile(boolean flush) throws IOException {
        if (recoveryFileStream != null) {
            recoveryFileStream.writeLong(currentPosition);
            if (flush) {
                recoveryFileStream.flush();
            }
        }
    }

    public void close() throws ParserException {
        if (reader == null) {
            throw new ParserException("No file open");
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (recoveryFileStream != null) {
            try {
                recoveryFileStream.close();
            } catch (IOException e) {
            }

            File recoveryFile = new File(this.filename+ recoveryFileExtention);
            recoveryFile.delete();
        }

        if (shutdownHook != null) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }

        this.filename = null;
        this.reader = null;
        this.recoveryFileStream = null;
        this.shutdownHook = null;
    }

    public Record[] parse(String fileName) throws ParserException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            throw new ParserException("File '"+fileName+"' not found.");
        }

        currentPosition = 0;

        File recoveryFile = new File(this.filename+ recoveryFileExtention);
        if (recoveryFile.exists()) {
            DataInputStream positionsFileReader;
            try {
                positionsFileReader = new DataInputStream(new FileInputStream(this.filename+ recoveryFileExtention));
            } catch (IOException e) {
                throw new ParserException("Unable to open recovery file '"+this.filename+ recoveryFileExtention +"' for reading", e);
            }

            try {
                for (;;)
                    currentPosition = positionsFileReader.readLong();
            } catch (IOException e) {
            }

            logger.debug("Recovered position: "+currentPosition);

            System.out.println("Recovered position: "+currentPosition);

            try {
                positionsFileReader.close();
            } catch (IOException e) {
            }

            for (int i = 0; i < currentPosition; i++) {
                try {
                    reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        ArrayList arrayList = new ArrayList();
        String buff = null;

        try {
            currentPosition++;
            buff = reader.readLine();
            Record record = null;
            while (buff != null) {
                if ((buff.length() > 0) && !isComment(buff)) {
                    try {
                        int nextFieldBegin = 0;
                        record = recordType.createRecord();
                        record.setSourceRecord(buff);
                        int fieldIndex = 0;
                        for (int i = 0; i < buff.length(); i++) {
                            if (buff.charAt(i) == token) {
                                recordType.setFieldValue(record, fieldIndex++, buff.substring(nextFieldBegin, i));
                                nextFieldBegin = i + 1;
                            }
                        }
                        recordType.setFieldValue(record, fieldIndex++, buff.substring(nextFieldBegin, buff.length()));
                        arrayList.add(record);
                    } catch (InvalidFieldException e) {
                        logger.error("InvalidFieldException. Skipping line "+currentPosition+" because "+e.getMessage());
                        recordType.releaseRecord(record);
                    } catch (InvalidFieldValueException e) {
                        logger.error("InvalidFieldValueException. Skipping line "+currentPosition+" because "+e.getMessage());
                        recordType.releaseRecord(record);
                    }
                    currentPosition++;
                }
                buff = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
            throw new ParserException("IOException", e);
        }

        Record[] retValue = new Record[0];
        retValue = (Record[]) arrayList.toArray(retValue);

        if ((recoveryFile != null) && (recoveryFile.exists())) {
            recoveryFile.delete();
        }

        return retValue;
    }

    public void releaseRecords(Record[] records) {
        for (int i = 0; i < records.length; i++) {
            releaseRecord(records[i]);
        }
    }

    public void releaseRecord(Record record) {
        recordType.releaseRecord(record);
    }

}
