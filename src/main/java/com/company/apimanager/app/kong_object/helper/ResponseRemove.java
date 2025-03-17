package com.company.apimanager.app.kong_object.helper;

import java.util.ArrayList;

public class ResponseRemove extends BaseTransformer {
    public ResponseRemove() {
    }

    public ResponseRemove(ArrayList<String> headers, ArrayList<String> json) {
        this.setHeaders(headers);
        this.setJson(json);
    }
}
