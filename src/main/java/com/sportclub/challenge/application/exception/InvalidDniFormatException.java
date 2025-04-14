package com.sportclub.challenge.application.exception;

public class InvalidDniFormatException extends RuntimeException{
    public InvalidDniFormatException(String message) {
        super(message);
    }
}
