package com.example.coffee.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="No such dish")  // 404
public class NotFoundException extends RuntimeException{
}
