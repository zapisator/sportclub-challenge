package com.sportclub.challenge.application.exception;

public class SportClubAccessDeniedException extends RuntimeException{
    public SportClubAccessDeniedException(String message) {
        super(message);
    }
}
