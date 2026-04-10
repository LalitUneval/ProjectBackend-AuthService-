package com.lalit.authservice.execption;

public class PasswordAlreadyUsedException extends RuntimeException {
    public PasswordAlreadyUsedException(String message) {
        super(message);
    }
}