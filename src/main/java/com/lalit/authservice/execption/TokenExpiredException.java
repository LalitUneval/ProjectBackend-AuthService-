package com.lalit.authservice.execption;



public class TokenExpiredException extends RuntimeException{
    public TokenExpiredException(String message)
    {
        super(message);
    }

}
