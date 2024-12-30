package org.example.request;

import java.io.Serializable;
import java.util.List;

public class Request implements Serializable {
    RequestType requestType;
    List<String> data;
    public Request(RequestType requestType, List<String> data) {
        this.requestType = requestType;
        this.data = data;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public List<String> getData() {
        return data;
    }
}
