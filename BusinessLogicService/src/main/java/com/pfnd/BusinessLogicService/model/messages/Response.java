package com.pfnd.BusinessLogicService.model.messages;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Getter
@Setter
@Slf4j
public class Response<T> {
    private String message;
    private Date date;
    private int code;
    private String stacktrace;
    private T containedObject;


    public Response(final String message, final int code, final String stacktrace, final T containedObject) {
        this.message = message;
        this.date = new Date();
        this.code = code;
        this.stacktrace = stacktrace;
        this.containedObject = containedObject;
    }

    @Override
    public String toString() {
        return "Response {" +
                "message='" + message + '\'' +
                ", date=" + date +
                ", code=" + code +
                ", stacktrace='" + stacktrace + '\'' +
                ", containedObject=" + containedObject +
                '}';
    }
}