package com.sportclub.challenge.adapter.out.log;

import com.sportclub.challenge.application.port.out.log.LoggingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Slf4jLoggingAdapter implements LoggingPort {

    private static final Logger log = LoggerFactory.getLogger(Slf4jLoggingAdapter.class);

    @Override
    public void trace(String message, Object... args) {
        if (log.isTraceEnabled()) {
            log.trace(message, args);
        }
    }

    @Override
    public void debug(String message, Object... args) {
        log.debug(message, args);
    }

    @Override
    public void info(String message, Object... args) {
        log.info(message, args);
    }

    @Override
    public void warn(String message, Object... args) {
        log.warn(message, args);
    }

    @Override
    public void error(String message, Throwable throwable, Object... args) {
        if (args.length > 0) {
            Object[] argsWithoutThrowable = new Object[args.length + 1];
            System.arraycopy(args, 0, argsWithoutThrowable, 0, args.length);
            argsWithoutThrowable[args.length] = throwable;
            log.error(message, argsWithoutThrowable);
        } else {
            log.error(message, throwable);
        }
    }
    @Override
    public void error(String message, Object... args) {
        log.error(message, args);
    }
}