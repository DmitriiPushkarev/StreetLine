package com.example.streetline;

public class AlreadyExistLoginException extends Exception {
    public AlreadyExistLoginException(String errorMessage) {
        super(errorMessage);
    }
}