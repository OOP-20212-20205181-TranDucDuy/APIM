package com.company.apimanager.app.kong_object.helper;


import java.util.ArrayList;

public class ResponseRename extends BaseTransformer {
    public ResponseRename() {
    }

    public ResponseRename(ArrayList<String> headers) {
        this.setHeaders(headers);
    }
}
