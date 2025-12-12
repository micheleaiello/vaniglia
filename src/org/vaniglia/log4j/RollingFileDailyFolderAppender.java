/**
 * Project Vaniglia
 * User: Patrizio Munzi
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

package org.vaniglia.log4j;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;
import org.vaniglia.utils.SynchronizedSimpleDateFormat;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

/**
 * This is a log4j appender that rolls the file daily and also based on the file dimention.
 *
 * ---> A example of properties configuration for this logger is the following:
 * log4j.appender.LogFile=org.vaniglia.log4j.RollingFileDailyFolderAppender
 * log4j.appender.LogFile.FileName=LogFile.log
 * log4j.appender.LogFile.DateFolderPattern='logs/'yyyy-MM-dd
 * log4j.appender.LogFile.MaxFileSize=1MB
 * log4j.appender.LogFile.MaxBackupIndex=10
 * log4j.appender.LogFile.updatePeriodMS=1000
 *
 * ---> Default values
 * log4j.appender.LogFile.FileName=file.log
 * log4j.appender.LogFile.DateFolderPattern=yyyy-MM-dd
 * log4j.appender.LogFile.MaxFileSize=10MB
 * log4j.appender.LogFile.MaxBackupIndex=1
 * log4j.appender.LogFile.updatePeriodMS=1000
 */
public class RollingFileDailyFolderAppender extends FileAppender {

    /**
     The default maximum file size is 10MB.
     */
    protected long maxFileSize = 10*1024*1024;

    /**
     There is one backup file by default.
     */
    protected int  maxBackupIndex  = 1;

    private String mstrFileName = "file.log";

    private String dateFolderPattern = "yyyy-MM-dd";

    private int updatePeriodMS = 1000;

    private long nextFilenameComputingMillis = System.currentTimeMillis () - 1;

    /**
     * The default constructor does no longer set a default layout nor a
     * default output target. This is required as the appender class is dynamically
     * loaded.
     */
    public
    RollingFileDailyFolderAppender() {
    }

    /**
     Instantiate a RollingFileAppender and open the file designated by
     <code>filename</code>. The opened filename will become the ouput
     destination for this appender.

     <p>If the <code>append</code> parameter is true, the file will be
     appended to. Otherwise, the file desginated by
     <code>filename</code> will be truncated before being opened.
     */
    public  RollingFileDailyFolderAppender (Layout layout,String filename,boolean append) throws IOException {
        super(layout, filename, append);
    }

    /**
     Instantiate a FileAppender and open the file designated by
     <code>filename</code>. The opened filename will become the output
     destination for this appender.

     <p>The file will be appended to. */
    public  RollingFileDailyFolderAppender (Layout layout,String filename) throws IOException {
        super(layout, filename);
    }


    /* (non-Javadoc)
      * @see org.apache.log4j.FileAppender#activateOptions()
      */
    public void activateOptions() {
        Date now = new Date();

        String foldersPath = SynchronizedSimpleDateFormat.getFormat(dateFolderPattern.trim()).format(now);
        createFolder(foldersPath);

        fileName = foldersPath + "/" + mstrFileName;

        super.activateOptions();
    }

    /*------------------------------------------------------------------------------
    * Getters
    *----------------------------------------------------------------------------*/

    public
    int getMaxBackupIndex() {
        return maxBackupIndex;
    }

    public
    long getMaximumFileSize() {
        return maxFileSize;
    }

    public String getDateFolderPattern() {
        return dateFolderPattern;
    }

    public String getFileName() {
        return mstrFileName;
    }

    public int getUpdatePeriodMS() {
        return updatePeriodMS;
    }


    /*------------------------------------------------------------------------------
    * Setters
    *----------------------------------------------------------------------------*/

    public
    void setMaxBackupIndex(int maxBackups) {
        this.maxBackupIndex = maxBackups;
    }


    public
    void setMaximumFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }


    public
    void setMaxFileSize(String value) {
        maxFileSize = OptionConverter.toFileSize(value, maxFileSize + 1);
    }

    public void setDateFolderPattern(String dateFolderPattern) {
        this.dateFolderPattern = dateFolderPattern;
    }


    public void setFileName(String mstrFileName) {
        this.mstrFileName = mstrFileName;
    }

    protected
    void setQWForFiles(Writer writer) {
        this.qw = new CountingQuietWriter(writer, errorHandler);
    }

    public void setUpdatePeriodMS(int upMs) {
        updatePeriodMS = upMs;
    }

    /*------------------------------------------------------------------------------
     * Methods
     *----------------------------------------------------------------------------*/

    private void createFolder(String foldersPath)
    {
        char chrEnd;
        File objFile;

        chrEnd = foldersPath.charAt(foldersPath.length() - 1);
        if (chrEnd == '\\' || chrEnd == '/')
            foldersPath = foldersPath.substring(0, (foldersPath.length() - 1));

        objFile = new File(foldersPath);
        // Ask file object to create all missing folders.
        objFile.mkdirs();
    }


    protected
    void subAppend(LoggingEvent event) {
        if( System.currentTimeMillis () >= nextFilenameComputingMillis ) {

            Date now = new Date();
            String newFoldersPath = SynchronizedSimpleDateFormat.getFormat(dateFolderPattern.trim()).format(now);
            String newFileName = newFoldersPath + "/" + mstrFileName;


            if ( !newFileName.equals(fileName)) {
                this.rollDateFolder(newFoldersPath, newFileName);
            }

            long current = System.currentTimeMillis();
            nextFilenameComputingMillis = current + updatePeriodMS;

        }
        else if ( (fileName != null) && ((CountingQuietWriter) qw).getCount() >= maxFileSize ) {
            this.rollFile();
        }

        super.subAppend(event);
    }


    private void rollDateFolder(String newFoldersPath, String newFileName) {
        createFolder(newFoldersPath);
        fileName = newFileName;

        try {
            // This will also close the file. This is OK since multiple
            // close operations are safe.
            this.setFile(fileName, false, bufferedIO, bufferSize);
        }
        catch(IOException e) {
            LogLog.error("setFile("+fileName+", false) call failed.", e);
        }
    }


    private void rollFile(){
        File target;
        File file;

        LogLog.debug("rolling over count=" + ((CountingQuietWriter) qw).getCount());
        LogLog.debug("maxBackupIndex="+maxBackupIndex);

        // If maxBackups <= 0, then there is no file renaming to be done.
        if(maxBackupIndex > 0) {
            // Delete the oldest file, to keep Windows happy.
            file = new File(fileName + '.' + maxBackupIndex);
            if (file.exists())
                file.delete();

            // Map {(maxBackupIndex - 1), ..., 2, 1} to {maxBackupIndex, ..., 3, 2}
            for (int i = maxBackupIndex - 1; i >= 1; i--) {
                file = new File(fileName + "." + i);
                if (file.exists()) {
                    target = new File(fileName + '.' + (i + 1));
                    LogLog.debug("Renaming file " + file + " to " + target);
                    file.renameTo(target);
                }
            }

            // Rename fileName to fileName.1
            target = new File(fileName + "." + 1);

            this.closeFile(); // keep windows happy.

            file = new File(fileName);
            LogLog.debug("Renaming file " + file + " to " + target);
            file.renameTo(target);
        }

        try {
            // This will also close the file. This is OK since multiple
            // close operations are safe.
            this.setFile(fileName, false, bufferedIO, bufferSize);
        }
        catch(IOException e) {
            LogLog.error("setFile("+fileName+", false) call failed.", e);
        }
    }
}

