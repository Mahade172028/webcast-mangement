package com.wtv.webcastmanagement.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@ControllerAdvice
@RestController
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(HandleWebcastException.class)
    public ResponseEntity<Object> handleWebcastException(HandleWebcastException ex, WebRequest request){
        CustomExceptionResponse customExceptionResponse = new CustomExceptionResponse(
                new Date(),ex.getMessage(),request.getDescription(false));
        return new ResponseEntity(customExceptionResponse, ex.getStatus());
    }
    @ExceptionHandler(HandleLegacyException.class)
    public ResponseEntity<Object> handleLegacyException(HandleLegacyException ex, WebRequest request){
        CustomExceptionResponse customExceptionResponse = new CustomExceptionResponse(
                new Date(),ex.getMessage(),request.getDescription(false));
        return new ResponseEntity(customExceptionResponse, ex.getStatus());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request){
        CustomExceptionResponse customExceptionResponse = new CustomExceptionResponse(
                new Date(),ex.getMessage(),request.getDescription(false));
        return new ResponseEntity(customExceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllGlobalException(Exception ex, WebRequest request){
        CustomExceptionResponse customExceptionResponse = new CustomExceptionResponse(
                new Date(),ex.getMessage(),request.getDescription(false));
        return new ResponseEntity(customExceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
