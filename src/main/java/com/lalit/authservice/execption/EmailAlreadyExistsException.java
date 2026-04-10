package com.lalit.authservice.execption;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message)
    {
        super(message);
    }

}
