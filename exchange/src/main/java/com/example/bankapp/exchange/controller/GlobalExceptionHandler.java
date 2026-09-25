package com.example.bankapp.exchange.controller;

import com.example.bankapp.exchange.exceptions.InvalidRateException;
import com.example.bankapp.exchange.exceptions.RateNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRateException.class)
    public ProblemDetail handleInvalidRate(InvalidRateException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(RateNotFoundException.class)
    public ProblemDetail handleRateNotFound(RateNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
