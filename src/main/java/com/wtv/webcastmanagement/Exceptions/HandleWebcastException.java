package com.wtv.webcastmanagement.Exceptions;

import org.springframework.http.HttpStatus;

public class HandleWebcastException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    public HandleWebcastException(String msg, HttpStatus status){
        super(msg);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
