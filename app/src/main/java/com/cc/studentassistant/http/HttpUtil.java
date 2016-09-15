package com.cc.studentassistant.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.Serializable;

/**
 * Created by CC on 2016/8/17.
 */
public class HttpUtil implements Serializable {

    private HttpClient httpClient;
    private String value;
    private String location;
    private String cookie;
    private String studentID;

    public HttpUtil() {
        httpClient = new HttpClientBySerializable();
    }

    public final HttpClient getHttpClient() {
        return httpClient;
    }

    public final String getValue() {
        return value;
    }

    public final void setValue(String value) {
        this.value = value;
    }

    public final String getLocation() {
        return location;
    }

    public final void setLocation(String location) {
        this.location = location;
    }

    public final String getStudentID() {
        return studentID;
    }

    public final void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public final String getCookie() {
        return cookie;
    }

    public final void setCookie(String cookie) {
        this.cookie = cookie;
    }

    private class HttpClientBySerializable extends DefaultHttpClient implements Serializable {
    }
}
