package com.company.apimanager.app.kong_object.helper;


import java.util.ArrayList;

public class RequestAppend extends BaseTransformer {
    public RequestAppend() {

    }

    public RequestAppend(ArrayList<String> body, ArrayList<String> headers, ArrayList<String> querystring) {
        this.setBody(body);
        this.setHeaders(headers);
        this.setQuerystring(querystring);
    }
}
