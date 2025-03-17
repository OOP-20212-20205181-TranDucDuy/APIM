package com.company.apimanager.app.kong_object.helper;

import java.util.ArrayList;

public class ResponseReplace extends BaseTransformer {
    public ResponseReplace() {
    }

    public ResponseReplace(ArrayList<String> headers, ArrayList<String> json, ArrayList<String> json_types) {
        this.setHeaders(headers);
        this.setJson(json);
        this.setJson_types(json_types);
    }
}
