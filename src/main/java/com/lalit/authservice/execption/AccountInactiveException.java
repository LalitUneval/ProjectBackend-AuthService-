package com.lalit.authservice.execption;

public class AccountInactiveException extends RuntimeException{
    public AccountInactiveException(String message)
    {
        super(message);
    }
}
