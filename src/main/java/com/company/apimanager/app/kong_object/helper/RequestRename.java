package com.company.apimanager.app.kong_object.helper;


import java.util.ArrayList;

public class RequestRename extends BaseTransformer {

    public RequestRename() {

    }
    public RequestRename(ArrayList<String> body, ArrayList<String> headers, ArrayList<String> querystring) {
        this.setBody(body);
        this.setHeaders(headers);
        this.setQuerystring(querystring);
    }
}
