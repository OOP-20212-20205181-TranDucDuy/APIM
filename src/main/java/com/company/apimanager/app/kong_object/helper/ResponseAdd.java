package com.company.apimanager.app.kong_object.helper;


import java.util.ArrayList;

public class ResponseAdd extends BaseTransformer {
    public ResponseAdd() {
    }

    public ResponseAdd(ArrayList<String> headers, ArrayList<String> json, ArrayList<String> json_types) {
        this.setHeaders(headers);
        this.setJson(json);
        this.setJson_types(json_types);
    }
}
