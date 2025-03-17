package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongServiceOAuth {
    private String kongId;
    private String provisionKey;

    public KongServiceOAuth() {

    }

    public void setKongId(String kongId) {
        this.kongId = kongId;
    }

    public String getKongId() {
        return kongId;
    }

    public String getProvisionKey() {
        return provisionKey;
    }

    public void setProvisionKey(String provisionKey) {
        this.provisionKey = provisionKey;
    }
}