package com.company.apimanager.app.kong_object.helper;


import java.util.ArrayList;

public class RequestReplace extends BaseTransformer {
    public RequestReplace() {

    }
    public RequestReplace(ArrayList<String> body, ArrayList<String> headers, ArrayList<String> querystring, String uri) {
        this.setBody(body);
        this.setHeaders(headers);
        this.setQuerystring(querystring);
    }
}
