package com.cagricelebi.datagw.lib.log;

import java.util.logging.Level;

public class Logger {

    private java.util.logging.Logger log;
    public static final Level DEFAULT_LEVEL = Level.INFO;
    public static final Level EXCEPTION_LEVEL = Level.SEVERE;

    Logger(String className) {
        this.log = java.util.logging.Logger.getLogger(className);
    }

    public static Logger getLogger(String className) {
        return new Logger(className);
    }

    public void log(String message) {
        log.log(DEFAULT_LEVEL, message);
    }

    public void log(Level level, String message) {
        log.log(level, message);
    }

    public void log(String message, Object... params) {
        if (message.contains("%s")) {
            log.log(DEFAULT_LEVEL, String.format(message, params));
        } else {
            log.log(DEFAULT_LEVEL, message, params);
        }
    }

    public void log(Exception e) {
        log.log(EXCEPTION_LEVEL, e.getMessage(), e);
    }

    public void log(String message, Exception e) {
        log.log(EXCEPTION_LEVEL, message, e);
    }

    public static String getTime() {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
            return sdf.format(java.util.Calendar.getInstance().getTime());
        } catch (Exception e) {
        }
        return "";
    }
}
