package com.company.apimanager.app.kong_object.helper;


import java.util.ArrayList;

public class RequestAdd extends BaseTransformer {

    public RequestAdd() {

    }
    public RequestAdd(ArrayList<String> body, ArrayList<String> headers, ArrayList<String> query) {
        this.setBody(body);
        this.setQuerystring(query);
        this.setHeaders(headers);
    }
}