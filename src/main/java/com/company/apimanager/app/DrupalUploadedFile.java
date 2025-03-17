package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class DrupalUploadedFile {
    String fId;
    String url;

    public DrupalUploadedFile() {

    }

    public String getfId() {
        return fId;
    }

    public void setfId(String fId) {
        this.fId = fId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
