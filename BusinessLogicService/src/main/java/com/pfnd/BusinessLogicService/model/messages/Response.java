package com.pfnd.BusinessLogicService.model.messages;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Data
@Slf4j
public class Response<T> {
    private Date date;
    private String error;
    private T containedObject;

    public Response(final T containedObject) {
        this.date = new Date();
        this.containedObject = containedObject;
    }

    public Response(final String stacktrace) {
        this.date = new Date();
        this.error = stacktrace;
    }
}