package com.sportclub.challenge.application.port.out.log;

public interface LoggingPort {
    void trace(String message, Object... args);
    void debug(String message, Object... args);
    void info(String message, Object... args);
    void warn(String message, Object... args);
    void error(String message, Throwable throwable, Object... args);
    void error(String message, Object... args);
}
