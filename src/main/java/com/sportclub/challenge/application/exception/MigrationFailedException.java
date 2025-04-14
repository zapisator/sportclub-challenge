package com.sportclub.challenge.application.exception;

public class MigrationFailedException extends RuntimeException{
    public MigrationFailedException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
