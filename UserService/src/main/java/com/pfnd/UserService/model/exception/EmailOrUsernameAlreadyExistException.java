package com.pfnd.UserService.model.exception;

public class EmailOrUsernameAlreadyExistException extends RuntimeException {
    public EmailOrUsernameAlreadyExistException(String message) {
        super(message);
    }
}
