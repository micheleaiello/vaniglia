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
package org.vaniglia.polling;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * The Directory Poller.
 * The Poller is a Thread that periodically checks a list of directory to see if new files have been added.
 * If there are new files all the registered EventListener are notified.
 */
public class DirectoryPoller extends Thread {

    public class AutomoveException extends Exception {

        private File origin;
        private File dest;

        AutomoveException(File origin, File dest, String msg) {
            super(msg);
            this.origin = origin;
            this.dest = dest;
        }

        /**
         * Return the poller associated to this exception.
         * @return the poller associated to this exception.
         */
        public DirectoryPoller getPoller() {
            return DirectoryPoller.this;
        }

        /**
         * Return the file to be moved
         * @return the file to be moved
         */
        public File getOrigin() {
            return origin;
        }

        /**
         * Return the destination file
         * @return the destination file
         */
        public File getDestination() {
            return dest;
        }
    }

    public class AutomoveDeleteException extends AutomoveException {
        AutomoveDeleteException(File origin, File dest, String msg) {
            super(origin, dest, msg);
        }
    }

    public class DefaultComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String str1 = (String) o1;
            String str2 = (String) o2;

            return str1.compareTo(str2);
        }
    }

    public static final String DEFAULT_AUTOMOVE_DIRECTORY = "received";
    public static final String DEFAULT_FINAL_DIRECTORY = "done";
    public static final String DEFAULT_EXCEPTION_DIRECTORY = "error";
    public static final String DEFAULT_EXCEPTION_DESCRIPTION_EXTENTION = "exc";

    private static int counter = 0;
    private boolean shutdownRequested = false;
    private FilenameFilter filter;
    private File[] dirs;
    private long[] baseTime;

    private boolean verbose = false;
    private boolean timeBasedOnLastLookup = true;

    protected List eventListenerList = new ArrayList();

    private boolean autoMove = false;

    private Map autoMoveDirs = new HashMap();
    private Map finalDirs = new HashMap();
    private Map exceptionDirs = new HashMap();

    private boolean appendTimestampToFinalNames = false;
    private boolean createExceptionDescriptionFile = false;

    private SimpleDateFormat appendedTimestampFormat = null;

    private String exceptionExt = DEFAULT_EXCEPTION_DESCRIPTION_EXTENTION;

    private FilenameFilter originalFilter;
    private long pollInterval = 10000;
    private boolean startBySleeping = false;

    private Comparator comparator = new DefaultComparator();
    private boolean sortFiles = false;

    private long modificationInterval = 1000;

    private int currentDir = -1;

    private boolean pause = false;

    private static class DirectoryFilter implements FilenameFilter {
        FilenameFilter additionalFilter;

        DirectoryFilter(FilenameFilter additionalFilter) {
            this.additionalFilter = additionalFilter;
        }

        public boolean accept(File dir, String name) {
            if (new File(dir, name).isDirectory()) return false;
            return additionalFilter.accept(dir, name);
        }

        public String toString() {
            return "Directory filter over a " + additionalFilter;
        }
    }

    private class TimeFilter implements FilenameFilter {
        private FilenameFilter additionalFilter;

        public TimeFilter(FilenameFilter additionalFilter) {
            this.additionalFilter = additionalFilter;
        }

        public boolean accept(File dir, String name) {
            File f = new File(dir, name);
            if (f.isDirectory()) return false;
            if (f.lastModified() <= baseTime[currentDir]) {
                if (verbose)
                    System.out.println(name + "(" + f.lastModified() + "): out of base time (" + baseTime[currentDir] + "), ignoring");
                return false;
            } else {
                if (verbose)
                    System.out.println(name + "(" + f.lastModified() + "): older than base time (" + baseTime[currentDir] + "), accepted");
            }

            return additionalFilter.accept(dir, name);
        }
    }

    public static final class NullFilenameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return true;
        }

        public String toString() {
            return "null filter";
        }
    }


    /**
     * Create a poller over the given directories, using the given filter.
     *
     * @param dirs an array of directories
     * @param filter a filter for files to look up
     */
    public DirectoryPoller(File[] dirs, FilenameFilter filter) {
        this(dirs, filter, false);
    }

    /**
     * Create a poller over the given directories, which will match any file.
     *
     * @param dirs an array of directories
     */
    public DirectoryPoller(File[] dirs) {
        this(dirs, new NullFilenameFilter());
    }


    /**
     * Create a poller over the given directory, using the given filter.
     *
     * @param directory a directory
     * @param filter a filter for files to look up
     */
    public DirectoryPoller(File directory, FilenameFilter filter) {
        this(new File[]{directory}, filter);
    }

    /**
     * Create a poller over the given directory, which will match any file.
     *
     * @param directory a directory
     */
    public DirectoryPoller(File directory) {
        this(new File[]{directory});
    }

    /**
     * Create a poller initially not bound to any directory, which uses the given filter.
     * Before starting the poller, a single call to setDirectories() must be done to bind the poller to a
     * specific directory.
     *
     * @param filter a filter for files to look up
     */
    public DirectoryPoller(FilenameFilter filter) {
        this(filter, false);
    }


    /**
     * Create a poller initially not bound to any directory, which will match any file.
     * Before starting the poller, a single call to setDirectories() must be done to bind the poller to a
     * specific directory.
     */
    public DirectoryPoller() {
        this(new NullFilenameFilter());
    }


    /**
     * Create a poller over the given directories, using the given filter and time-based filtering.
     *
     * @param dirs an array of directories
     * @param filter a filter for files to look up
     * @param timeBased if <i>true</i>, the poller uses time-based lookup
     */
    public DirectoryPoller(File[] dirs, FilenameFilter filter, boolean timeBased) {
        setName("directory-poller-" + (counter++));
        setDirectories(dirs);
        this.originalFilter = new DirectoryFilter(filter);
        setTimeBased(timeBased);
        this.baseTime = new long[dirs.length];
    }

    /**
     * Create a poller over the given directory, using the given filter and time-based filtering.
     *
     * @param directory a directory
     * @param filter a filter for files to look up
     */
    public DirectoryPoller(File directory, FilenameFilter filter, boolean timeBased) {

        this(new File[]{directory}, filter, timeBased);
    }

    /**
     * Create a poller initially not bound to any directory, which uses the given filter and time-based filtering.
     * Before starting the poller, a single call to setDirectories() must be done to bind the poller to a
     * specific directory.
     *
     * @param filter a filter for files to look up
     */
    public DirectoryPoller(FilenameFilter filter, boolean timeBased) {
        this(new File[0], filter, timeBased);
    }

    /**
     * Add one directory to the controlled set. It can be called only if the poller thread hasn't started yet.
     *
     * @param dir the directory to add
     *
     * @exception java.lang.IllegalStateException if the poller has already started.
     * @exception java.lang.IllegalArgumentException if String does not contain a directory path
     */
    public void addDirectory(File dir) {
        File[] originalDirs = getDirectories();
        File[] dirs = new File[getDirectories().length + 1];
        System.arraycopy(originalDirs, 0, dirs, 0, originalDirs.length);
        dirs[originalDirs.length] = dir;
        setDirectories(dirs);
    }

    /**
     * Remove one directory from the controlled set. It can be called only if the poller thread hasn't started yet.
     *
     * @param dir the directory to remove
     *
     * @exception java.lang.IllegalStateException if the poller has already started.
     * @exception java.lang.IllegalArgumentException if the directory is not among the controlled ones
     */
    public void removeDirectory(File dir) {
        File[] originalDirs = getDirectories();
        File[] dirs = new File[originalDirs.length - 1];
        boolean removed = false;
        int c = 0;
        for (int i = 0; i < originalDirs.length; i++) {
            if (originalDirs[i].equals(dir)) {
                removed = true;
            } else {
                dirs[c++] = originalDirs[i];
            }
        }
        if (!removed)
            throw new IllegalArgumentException(dir + " is not a controlled directory");
        setDirectories(dirs);
    }


    /**
     * Set the directories controlled by the poller. It can be called only if the poller thread hasn't started yet.
     *
     * @param dirs the directories to be controlled by the poller
     *
     * @exception java.lang.IllegalStateException if the poller has already started.
     * @exception java.lang.IllegalArgumentException if any of the File objects is not a directory
     */
    public void setDirectories(File[] dirs) {
        if (isAlive())
            throw new IllegalStateException("Can't call setDirectories when the poller has already started");
        if (dirs != null) {
            for (int i = 0; i < dirs.length; i++) {
                if (!dirs[i].isDirectory())
                    throw new IllegalArgumentException(dirs[i] + " is not a directory");
            }
        }
        this.dirs = dirs;
        baseTime = new long[dirs.length];
    }

    /**
     * Return the directories controlled by the poller.
     *
     * @return the directories controlled by the poller
     */
    public File[] getDirectories() {
        return dirs;
    }

    /**
     * Instruct the poller to automatically move the file to the directory associated to each directory under control,
     * which can be set/retrieved by setAutoMoveDirectory()/getAutoMoveDirectory()
     *
     * @param autoMove if <i>true</i>, the poller will automatically move selected files in the "received" directory associated
     * to each directory under control
     */
    public void setAutoMove(boolean autoMove) {
        this.autoMove = autoMove;
    }

    /**
     * Returns the autoMove mode.
     *
     * @return <i>true</i> if autoMove mode is active
     */
    public boolean getAutoMove() {
        return autoMove;
    }


    public boolean isAppendTimestampToFinalNames() {
        return appendTimestampToFinalNames;
    }

    public void setAppendTimestampToFinalNames(boolean appendTimestampToFinalNames) {
        this.appendTimestampToFinalNames = appendTimestampToFinalNames;
    }

    public String getAppendedTimestampFormat() {
        if (appendedTimestampFormat != null) {
            return appendedTimestampFormat.toPattern();
        }
        else {
            return null;
        }
    }

    public void setAppendedTimestampFormat(String appendedTimestampFormatPattern) {
        try {
            this.appendedTimestampFormat = new SimpleDateFormat(appendedTimestampFormatPattern);
        } catch (Exception e) {
            e.printStackTrace();
            this.appendedTimestampFormat = null;
        }
    }

    public boolean isCreateExceptionDescriptionFile() {
        return createExceptionDescriptionFile;
    }

    public void setCreateExceptionDescriptionFile(boolean createExceptionDescriptionFile) {
        this.createExceptionDescriptionFile = createExceptionDescriptionFile;
    }

    public String getExceptionExtention() {
        return exceptionExt;
    }

    public void setExceptionExtention(String extention) {
        this.exceptionExt = extention;
    }

    /**
     * Returns the directory associated to the given controlled directory,
     * where files polled are automatically moved if the autoMove mode is active.
     *
     * If no directory is associated by setAutoMoveDirectory(), the subdirectory DEFAULT_AUTOMOVE_DIRECTORY is
     * associated automatically.
     *
     * @param directory the directory for which the associated "automove" directory is requested
     * @exception java.lang.IllegalArgumentException if <tt>directory</tt> is not under control of the poller
     */
    public File getAutoMoveDirectory(File directory) throws IllegalArgumentException {
        directory = PathNormalizer.normalize(directory);
        File f = (File) autoMoveDirs.get(directory);
        if (f == null) {
            f = new File(directory, DEFAULT_AUTOMOVE_DIRECTORY);
            _setAutoMoveDirectory(directory, f);
        }
        return f;
    }

    public File getFinalDirectory(File directory) throws IllegalArgumentException {
        directory = PathNormalizer.normalize(directory);
        File f = (File) finalDirs.get(directory);
        if (f == null) {
            f = new File(directory, DEFAULT_FINAL_DIRECTORY);
            _setFinalDirectory(directory, f);
        }
        return f;
    }

    public File getExceptionDirectory(File directory) throws IllegalArgumentException {
        directory = PathNormalizer.normalize(directory);
        File f = (File) exceptionDirs.get(directory);
        if (f == null) {
            f = new File(directory, DEFAULT_EXCEPTION_DIRECTORY);
            _setExceptionDirectory(directory, f);
        }
        return f;
    }

    /**
     * Associate a directory to one of the controlled directories, for the autoMove mode.
     *
     * This method can be called only if the poller has not started yet.
     *
     * @param directory the controlled directory
     * @param autoMoveDirectory the directory associated to the controlled directory
     *
     * @exception java.lang.IllegalArgumentException if <tt>directory</tt> is not a controlled directory
     * @exception java.lang.IllegalStateException
     */
    public void setAutoMoveDirectory(File directory, File autoMoveDirectory)
            throws IllegalArgumentException, IllegalStateException
    {
        if (isAlive()) {
            throw new IllegalStateException("auto-move directories cannot be set once the poller has started");
        }
        _setAutoMoveDirectory(directory, autoMoveDirectory);
    }

    // This version is called internally from execute(), when the thread is alive
    private void _setAutoMoveDirectory(File directory, File autoMoveDirectory)
            throws IllegalArgumentException
    {
        directory = PathNormalizer.normalize(directory);
        checkIfManaged(directory);
        autoMoveDirs.put(directory, directory = PathNormalizer.normalize(autoMoveDirectory));
    }

    public void setFinalDirectory(File directory, File finalDirectory)
            throws IllegalArgumentException, IllegalStateException
    {
        if (isAlive()) {
            throw new IllegalStateException("final directories cannot be set once the poller has started");
        }
        _setFinalDirectory(directory, finalDirectory);
    }

    private void _setFinalDirectory(File directory, File finalDirectory)
            throws IllegalArgumentException
    {
        directory = PathNormalizer.normalize(directory);
        checkIfManaged(directory);
        finalDirs.put(directory, directory = PathNormalizer.normalize(finalDirectory));
    }

    public void setExceptionDirectory(File directory, File exceptionDirectory)
            throws IllegalArgumentException, IllegalStateException
    {
        if (isAlive()) {
            throw new IllegalStateException("final directories cannot be set once the poller has started");
        }
        _setExceptionDirectory(directory, exceptionDirectory);
    }

    private void _setExceptionDirectory(File directory, File exceptionDirectory)
            throws IllegalArgumentException
    {
        directory = PathNormalizer.normalize(directory);
        checkIfManaged(directory);
        exceptionDirs.put(directory, directory = PathNormalizer.normalize(exceptionDirectory));
    }

    public boolean isSortFiles() {
        return sortFiles;
    }

    public void setSortFiles(boolean sortFiles) {
        this.sortFiles = sortFiles;
    }

    public Comparator getComparator() {
        return comparator;
    }

    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }

    protected void checkIfManaged(File directory) {
        for (int i = 0; i < dirs.length; i++) {
            if (PathNormalizer.normalize(dirs[i]).equals(directory)) {
                return;
            }
        }

        throw new IllegalArgumentException("The directory " + directory +
                " is not under control of the directory poller");
    }


    /**
     * Set the verbose level. Verbosity is mainly for debugging/tracing purposes,
     * since the poller delivers events to any listener, which can therefore perform the
     * tracing.
     *
     * @param verbose if true, the poller logs to system out.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Return the verbose flag.
     *
     * @return the verbose flag
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Sets the usage of time-based filtering (besides the normal filtering).
     * It can be called only if the poller thread hasn't started yet.
     *
     * @param timeBased if <i>true</i> the poller will use time-based filtering.
     *
     * @exception java.lang.IllegalStateException if the poller has already started.
     */
    public void setTimeBased(boolean timeBased) {
        if (timeBased) {
            if (filter != null && isTimeBased()) return;
            this.filter = new TimeFilter(originalFilter);
        } else {
            if (filter != null && !isTimeBased()) return;
            this.filter = originalFilter;
        }
    }

    /**
     * Return <i>true</i> if the poller is using time-based filtering.
     *
     * @return <i>true</i> if the poller is using time-based filtering.
     */
    public boolean isTimeBased() {
        return (filter instanceof TimeFilter);
    }

    /**
     * Reset the base time for the given directory.
     *
     * It's irrelevant if time-based filtering is not enabled.
     *
     * @param directory the directory for which to set the base time
     * @param time the new base time
     *
     * @exception java.lang.IllegalArgumentException if the given directory is not under control of the poller
     */
    public void setBaseTime(File directory, long time) {
        for (int i = 0; i < dirs.length; i++)
            if (dirs[i].getAbsolutePath().equals(directory.getAbsolutePath())) {
                baseTime[i] = time;
                return;
            }
        throw new IllegalArgumentException("'" + directory + "' is not under control of the poller");
    }

    /**
     * Reset the base time for the all the directories under control of the poller.
     *
     * It's irrelevant if time-based filtering is not enabled.
     *
     * @param time the new base time
     */
    public void setBaseTime(long time) {
        for (int i = 0; i < dirs.length; i++)
            setBaseTime(dirs[i], time);
    }

    /**
     * Return the current base time for the given directory.
     *
     * The returned value is unpredictable if time-based filtering is not enabled.
     *
     * @param directory the directory for which to get the base time
     *
     * @exception java.lang.IllegalArgumentException if the given directory is not under control of the poller
     *
     * @return the current base time, if time-based filtering is enabled
     */
    public long getBaseTime(File directory) {
        for (int i = 0; i < dirs.length; i++) {
            if (dirs[i].getAbsolutePath().equals(directory.getAbsolutePath())) return baseTime[i];
        }

        throw new IllegalArgumentException("'" + directory + "' is not under control of the poller");
    }


    /**
     * Return <i>true</i> if the poller is time based, and uses last-polling time
     * as a basis for the lookup.
     */
    public boolean isPollingTimeBased() {
        return isTimeBased() && timeBasedOnLastLookup;
    }

    /**
     * Sets the subtype of time-based filtering used by the poller.
     * <p>
     * For the call to have any meaning, the poller must be in time-based mode, that is,
     * {@link org.vaniglia.polling.DirectoryPoller#setTimeBased(boolean) setTimeBased()} must have been
     * called with <b>true</b> as parameter (se class comment): if the parameter
     * is <b>true</b>, the poller will select only files older than the last
     * polling time (besides applying any user-defined filter); if the parameter
     * is <b>false</b>, the poller will select only files <i>whose last modification
     * time is higher than the higher last modification time found in the last
     * polling cycle</i>.
     *
     * @param v determines the time-based filtering subtype
     */
    public void setPollingTimeBased(boolean v) {
        timeBasedOnLastLookup = v;
    }

    /**
     * Return the current poll interval. See class comments for notes.
     * @return the current poll interval
     */
    public long getPollInterval() {
        return pollInterval;
    }

    /**
     * Set the poll interval. The poller sleeps for <tt>pollInterval</tt> milliseconds
     * and then performs a lookup in the bound directories. See class comments for notes.
     *
     * @param pollInterval the poll interval
     */
    public void setPollInterval(long pollInterval) {
        this.pollInterval = pollInterval;
    }

    /**
     * Instruct the poller whether to start by sweeping the controlled directories,
     * or by going immediatly to sleep.
     * @param v if <b>true</b> the poller starts by going immediatly to sleep
     */
    public void setStartBySleeping(boolean v) {
        startBySleeping = v;
    }


    /**
     * Return if the poller starts by sweeping the controlled directories,
     * or going immediatly to sleep.
     * @return if the poller starts by sweeping the controlled directories,
     * or going immediatly to sleep.
     */
    public boolean isStartBySleeping() {
        return startBySleeping;
    }

    /**
     * Sets the modification interval.
     * This parameter is used to avoid processing file that are still been produced by other systems.
     * The poller checks the last modification date of the file and if is within the modification interval
     * than the file is not processed.
     *
     * @param modInterval modification interval in milliseconds.
     */
    public void setModificationInterval(long modInterval) {
        this.modificationInterval = modInterval;
    }

    public long getModificationInterval() {
        return this.modificationInterval;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public boolean isPause() {
        return this.pause;
    }

    /**
     * Adds on {@link org.vaniglia.polling.FileEventListener FileEventListener} to the poller,
     * which will be notified on polling events.
     * <p>
     * You may add many poll managers, but there is no support for
     * inter-poll manager coordination, so if a FileEventListener deletes/moves
     * a polled file, the others will still receive the associated
     * event, but may not be able to perform proper processing.
     */
    public void addEventListener(FileEventListener pm) {
        eventListenerList.add(pm);
    }

    /**
     * Request the poller to shut down.
     * <p>
     * If a notification is in course, the notification is completed before
     * shutting down. Therefore, the event handler may want to check whether a
     * shutdown has been requested before reacting to an event.
     */
    public void shutdown() {
        shutdownRequested = true;
        this.interrupt();
        if (verbose)
            System.out.println("Polling shutdown requested");
    }

    /**
     * Return true if a shutdown has been requested.
     * @return true if a shutdown has been requested.
     */
    public boolean isShuttingDown() {
        return shutdownRequested;
    }

    /**
     * Invoked when the thread is started.
     *
     * Performs a polling, notifying the registered listeners of
     * related events. After each nofication, a check is done
     * of whether a shutdown has been requested or not.
     */
    public synchronized void run() {
        shutdownRequested = false;

        if (dirs == null) {
            throw new IllegalStateException("Programming error: no directories to poll specified");
        }

        if (verbose) {
            System.out.println("Polling started, interval is " + pollInterval + "ms");
        }

        if (autoMove) {
            // Try to create the automove dirs
            for (int j = 0; j < dirs.length; j++) {
                File automoveDir = PathNormalizer.normalize(getAutoMoveDirectory(dirs[j]));
                if (!automoveDir.exists()) {
                    if (verbose)
                        System.out.println("Automove directory " + automoveDir + " does not exist, attempting to create.");
                    if (!automoveDir.mkdirs()) throw new RuntimeException("Could not create the directory " + automoveDir.getAbsolutePath());
                    if (verbose)
                        System.out.println("Automove directory " + automoveDir + " created successfully.");
                }
            }
        }

        if (autoMove) {
            // Try to create the automove dirs
            for (int j = 0; j < dirs.length; j++) {
                File finalDir = PathNormalizer.normalize(getFinalDirectory(dirs[j]));
                if (!finalDir.exists()) {
                    if (verbose)
                        System.out.println("Automove directory " + finalDir + " does not exist, attempting to create.");
                    if (!finalDir.mkdirs()) throw new RuntimeException("Could not create the directory " + finalDir.getAbsolutePath());
                    if (verbose)
                        System.out.println("Automove directory " + finalDir + " created successfully.");
                }
            }
        }

        if (autoMove) {
            // Try to create the automove dirs
            for (int j = 0; j < dirs.length; j++) {
                File exDir = PathNormalizer.normalize(getExceptionDirectory(dirs[j]));
                if (!exDir.exists()) {
                    if (verbose)
                        System.out.println("Automove directory " + exDir + " does not exist, attempting to create.");
                    if (!exDir.mkdirs()) throw new RuntimeException("Could not create the directory " + exDir.getAbsolutePath());
                    if (verbose)
                        System.out.println("Automove directory " + exDir + " created successfully.");
                }
            }
        }

        long lastTimestamp;
        long now;
        long toSleep;
        // Main loop
        do {
            lastTimestamp = System.currentTimeMillis();
            // Immediatly go to sleep if startBySleeping==true...
            if (startBySleeping) {
                startBySleeping = false; // ..and pop it so we dont *only* sleep.
            } else {
                if (!pause) {
                    runCycle();
                }
            }

            // Go to sleep
            if (!shutdownRequested)
                try {
                    now = System.currentTimeMillis();
                    toSleep = pollInterval - (now - lastTimestamp);
                    if (toSleep > 0) {
                        sleep(toSleep);
                    }
                    if (verbose) {
                        System.out.println("Poller waking up");
                    }
                } catch (InterruptedException e) {
                    //System.out.println("Sleep interrupted");
                }
        } while (!shutdownRequested);

        if (verbose) {
            System.out.println("Notifying shutdown.");
        }
        notifyShutdown();

        if (verbose) {
            System.out.println("Poller terminated.");
        }
    }

    private void runCycle() {
        // URGENT the recovery from the working dir shall be executed only on the first cycle.
        // URGENT if the file can't be moved from the working dir it will be re-executed.

        // Notify wakeup
        if (!shutdownRequested)

            // Initiate directories lookup. currentDir is a member
            // used also by the TimeFilenameFilter
            if (!shutdownRequested)
                for (currentDir = 0; currentDir < dirs.length; currentDir++) {
                    File dir = PathNormalizer.normalize(dirs[currentDir]);
                    File originalDir = dir;

                    // Notify directory lookup start
                    if (shutdownRequested) return;

                    long filesLookupTime = System.currentTimeMillis();
                    // Get the files

                    boolean recovering = false;
                    String[] files = dir.list(filter);

                    if (autoMove) {
                        File workingDir = getAutoMoveDirectory(dir);
                        String[] workingFiles = workingDir.list(filter);
                        if (workingFiles.length > 0) {
                            recovering = true;
                            files = workingFiles;
                        }
                    }

                    if ((sortFiles) && (files != null)) {
                        //Sort the files in the directory using the comparator provided.
                        Arrays.sort(files, comparator);
                    }

                    boolean[] modified = new boolean[files.length];
                    int modifiedCount = 0;
                    Arrays.fill(modified, false);

                    // Check for files modified in the last x millis.
                    long current = System.currentTimeMillis();
                    for (int j = 0; j < files.length; j++) {
                        File file = new File(dir,  files[j]);
                        long lastModified = file.lastModified();
                        if (lastModified > (current - modificationInterval)) {
                            modified[j] = true;
                            modifiedCount++;
                            if (verbose) {
                                System.out.println("File " + files[j] + " has been modified in the last "+modificationInterval + "ms and will not be polled.");
                            }
                        }
                    }

                    String[] filesTmp = new String[files.length - modifiedCount];
                    int idx = 0;
                    for (int k = 0; k < files.length; k++) {
                        if (!modified[k]) {
                            filesTmp[idx++] = files[k];
                        }
                    }
                    files = filesTmp;

                    // If autoMove, then move the files in their destination
                    // directory
                    String[] movedFiles = new String[files.length]; // Only for autoMove mode
                    int failedToMoveCount = 0;
                    File autoMoveDir = getAutoMoveDirectory(dir);
                    if ((autoMove) && (!recovering)) {
                        for (int j = 0; j < files.length; j++) {
                            File orig = new File(dir, files[j]);
                            File dest = new File(autoMoveDir, files[j]);

                            if (dest.exists()) {
                                // Delete the existing file. Notify if failed.
                                if (verbose)
                                    System.out.println("[Automove] Attempting to delete existing " + dest.getAbsolutePath());
                                if (!dest.delete()) {
                                    notifyExceptionDeletingTargetFile(new ExceptionEvent(new AutomoveDeleteException(orig, dest, "Could not delete " + dest.getAbsolutePath()), this));
                                    failedToMoveCount++;
                                    continue;
                                } else if (verbose)
                                    System.out.println("[Automove] Deleted " + dest.getAbsolutePath());
                            }

                            // Move the file - notify the listeners if an exception occurs
                            if (verbose)
                                System.out.println("[Automove] Moving " + orig.getAbsolutePath() + " to " + autoMoveDir.getAbsolutePath() + File.separator);

                            autoMoveDir.mkdirs();
                            if (!orig.renameTo(dest)) {
                                notifyExceptionMovingFile(new ExceptionEvent(new AutomoveException(orig, dest, "Could not move " + orig.getName() + " to " + dest.getAbsolutePath()), this));
                                failedToMoveCount++;
                            } else {
                                //movedFiles[j]=autoMoveDir.getAbsolutePath()+File.separator+dest.getName();
                                notifyFileMoved(new FileMovedEvent(this, orig, dest));
                                movedFiles[j] = dest.getName();
                                if (j + 1 == files.length) dir = autoMoveDir;
                                if (verbose)
                                    System.out.println("[Automove] Moved " + orig.getAbsolutePath() + " to " + autoMoveDir.getAbsolutePath() + File.separator);
                            }
                        }
                    }

                    if ((autoMove) && (!recovering)) {
                        // Shrink the array if needed, to avoid nulls due to files which
                        // have failed to move
                        String[] tmp = new String[files.length - failedToMoveCount];
                        int c = 0;
                        for (int i = 0; i < movedFiles.length; i++) {
                            if (movedFiles[i] != null) {
                                tmp[c++] = movedFiles[i];
                            }
                        }
                        files = tmp;
                    }

                    if (recovering) {
                        dir = autoMoveDir;
                    }

                    // Notify each file
                    movedFiles = new String[files.length]; // Only for autoMove mode
                    failedToMoveCount = 0;
                    File stdFinalDir = getFinalDirectory(originalDir);
                    File exFinalDir = getExceptionDirectory(originalDir);

                    for (int j = 0; j < files.length; j++) {
                        Throwable exception = null;
                        File file = new File(dir, files[j]);
                        try {
                            notifyFileFound(new FileFoundEvent(this, file));
                        } catch (Throwable e) {
                            exception = e;
                        }

                        if (autoMove) {
                            File finalDir;
                            if (exception != null) {
                                finalDir = exFinalDir;
                            }
                            else {
                                finalDir = stdFinalDir;
                            }

                            File orig = new File(autoMoveDir, files[j]);

                            File dest;
                            if (appendTimestampToFinalNames) {
                                String fileName;
                                int indexOfDot = files[j].lastIndexOf('.');
                                String timestamp;
                                if (appendedTimestampFormat != null) {
                                    timestamp = appendedTimestampFormat.format(new Date(System.currentTimeMillis()));
                                }
                                else {
                                    timestamp = String.valueOf(System.currentTimeMillis());
                                }
                                if (indexOfDot > 0) {
                                    fileName = files[j].substring(0, indexOfDot)+'-'+timestamp+files[j].substring(indexOfDot, files[j].length());
                                }
                                else {
                                    fileName = files[j]+'-'+timestamp;
                                }
                                dest = new File(finalDir, fileName);
                            } else {
                                dest = new File(finalDir, files[j]);
                            }

                            if (dest.exists()) {
                                // Delete the existing file. Notify if failed.
                                if (verbose)
                                    System.out.println("[Automove] Attempting to delete existing " + dest.getAbsolutePath());
                                if (!dest.delete()) {
                                    notifyExceptionDeletingTargetFile(new ExceptionEvent(new AutomoveDeleteException(orig, dest, "Could not delete " + dest.getAbsolutePath()), this));
                                    failedToMoveCount++;
                                    continue;
                                } else if (verbose)
                                    System.out.println("[Automove] Deleted " + dest.getAbsolutePath());
                            }

                            // Move the file - notify the listeners if an exception occurs
                            if (verbose)
                                System.out.println("[Automove] Moving " + orig.getAbsolutePath() + " to " + finalDir.getAbsolutePath() + File.separator);

                            if ((createExceptionDescriptionFile) && (exception != null)) {
                                File exDescFile = new File(finalDir, dest.getName()+'.'+exceptionExt);
                                try {
                                    FileWriter fileWriter = new FileWriter(exDescFile);
                                    PrintWriter writer = new PrintWriter(fileWriter);
                                    exception.printStackTrace(writer);
                                    writer.close();
                                    fileWriter.close();
                                } catch (IOException e) {
                                    //TODO
                                }
                            }

                            finalDir.mkdirs();
                            if (!orig.renameTo(dest)) {
                                notifyExceptionMovingFile(new ExceptionEvent(new AutomoveException(orig, dest, "Could not move " + orig.getName() + " to " + dest.getAbsolutePath()), this));
                                failedToMoveCount++;
                            } else {
                                notifyFileMoved(new FileMovedEvent(this, orig, dest));
                                movedFiles[j] = dest.getName();
                                if (j + 1 == files.length) dir = finalDir;
                                if (verbose)
                                    System.out.println("[Automove] Moved " + orig.getAbsolutePath() + " to " + finalDir.getAbsolutePath() + File.separator);
                            }
                        }

                        if (shutdownRequested) {
                            break;
                        }
                    }

                    if (autoMove) {
                        // Shrink the array if needed, to avoid nulls due to files which
                        // have failed to move
                        String[] tmp = new String[files.length - failedToMoveCount];
                        int c = 0;
                        for (int i = 0; i < movedFiles.length; i++)
                            if (movedFiles[i] != null)
                                tmp[c++] = movedFiles[i];
                        files = tmp;
                    }

                    // Make sure that baseTime is set to the higher modified time
                    // of the files being read

                    if (isTimeBased()) {

                        if (verbose) System.out.println("Computing new base time");
                        // compute new base time, depending on the working mode
                        if (timeBasedOnLastLookup) {

                            baseTime[currentDir] = filesLookupTime; // Last lookup time
                        } else {

                            for (int j = 0; j < files.length; j++) { // Highest file time
                                File file = new File(dir, files[j]);
                                long lastModifiedTime = file.lastModified();
                                if (lastModifiedTime > baseTime[currentDir]) {
                                    baseTime[currentDir] = lastModifiedTime;
                                }
                            }
                            if (verbose)
                                System.out.println("Basetime for " + dirs[currentDir] + " is " + baseTime[currentDir]);
                        }

                    }

                    if (shutdownRequested) return;

                }
    }

    /**
     * Get the current filter
     */
    public FilenameFilter getFilter() {
        return filter;
    }

    /**
     * Set the current filter. This can be invoked only when the
     * poller is not running.
     * @param filter the new filename filter to use.
     */
    public void setFilter(FilenameFilter filter) {
        if (isAlive())
            throw new IllegalStateException("Can't call setFilter when the poller has already started");
        this.filter = filter;
    }

    private void notifyFileMoved(FileMovedEvent event) {
        for (int i = 0; i < eventListenerList.size(); i++) {
            ((FileEventListener) eventListenerList.get(i)).fileMoved(event);
        }
    }

    private void notifyFileFound(FileFoundEvent event) throws Exception {
        for (int i = 0; i < eventListenerList.size(); i++) {
            ((FileEventListener) eventListenerList.get(i)).fileFound(event);
        }
    }

    private void notifyExceptionDeletingTargetFile(ExceptionEvent exEvent) {
        for (int i = 0; i < eventListenerList.size(); i++) {
            ((FileEventListener) eventListenerList.get(i)).exceptionDeletingTargetFile(exEvent);
        }
    }

    private void notifyExceptionMovingFile(ExceptionEvent exEvent) {
        for (int i = 0; i < eventListenerList.size(); i++) {
            ((FileEventListener) eventListenerList.get(i)).exceptionMovingFile(exEvent);
        }
    }

    private void notifyShutdown() {
        for (int i = 0; i < eventListenerList.size(); i++) {
            ((FileEventListener) eventListenerList.get(i)).shutdown();
        }
    }

}