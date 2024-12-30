package org.example.request;

import java.io.Serializable;
import java.util.List;

public class Request implements Serializable {
    RequestType requestType;
    List<String> data;
    String countryCode;

    public Request(
            RequestType requestType,
            List<String> data,
            String countryCode
    ) {
        this.requestType = requestType;
        this.data = data;
        this.countryCode = countryCode;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public List<String> getData() {
        return data;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
