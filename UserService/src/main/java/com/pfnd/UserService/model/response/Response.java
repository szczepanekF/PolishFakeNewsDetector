package com.pfnd.UserService.model.response;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Getter
@Setter
@Slf4j
public class Response<T> {
    private Date date;
    private String error;
    private T containedObject;


    public Response(final T containedObject) {
        this.date = new Date();
        this.error = "";
        this.containedObject = containedObject;
    }

    public Response(final String error) {
        this.date = new Date();
        this.error = error;
        this.containedObject = null;
    }

    public Response(final String error,final T containedObject) {
        this.date = new Date();
        this.error = error;
        this.containedObject = containedObject;
    }
    @Override
    public String toString() {
        return "Response {" +
                ", date=" + date +
                ", error='" + error + '\'' +
                ", containedObject=" + containedObject +
                '}';
    }
}
