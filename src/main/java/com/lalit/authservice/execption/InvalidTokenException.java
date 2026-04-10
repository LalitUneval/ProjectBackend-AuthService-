package com.lalit.authservice.execption;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(String message)
    {
        super(message);
    }

}
