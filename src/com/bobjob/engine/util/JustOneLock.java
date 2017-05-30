package com.bobjob.engine.util;

import java.io.*;
import java.nio.channels.*;

public class JustOneLock {
    private String appName;
    private File file;
    private FileChannel channel;
    private FileLock lock;

    public JustOneLock(String appName) {
        this.appName = appName;
    }

    public boolean isAppActive() {
        try {
            file = new File
                 (System.getProperty("user.home"), appName + ".tmp");
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            channel = raf.getChannel();

            try {
                lock = channel.tryLock();
            }
            catch (OverlappingFileLockException e) {
                // already locked
                closeLock();
                return true;
            }

            if (lock == null) {
                closeLock();
                return true;
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                    // destroy the lock when the JVM is closing
                    public void run() {
                        closeLock();
                        deleteFile();
                    }
                });
            return false;
        }
        catch (Exception e) {
            closeLock();
            
            return true;
        }
    }

    public void closeLock() {
        try { lock.release();  }
        catch (Exception e) {  }
        try { channel.close(); }
        catch (Exception e) {  }
    }

    public void deleteFile() {
        try { file.delete(); }
        catch (Exception e) { }
    }
}