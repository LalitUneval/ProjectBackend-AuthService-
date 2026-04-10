package com.lalit.authservice.execption;

public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException(String message)
    {
        super(message);
    }

}
