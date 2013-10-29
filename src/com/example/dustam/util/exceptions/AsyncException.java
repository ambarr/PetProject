package com.example.dustam.util.exceptions;

public class AsyncException extends Exception {
    public AsyncException(String msg, Exception cause) {
        super(msg, cause);
    }
}