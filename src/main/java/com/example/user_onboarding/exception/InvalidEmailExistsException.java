package com.example.user_onboarding.exception;

public class InvalidEmailExistsException extends RuntimeException{
    public InvalidEmailExistsException(String message){
        super(message);
    }
}
