package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongConsumerPlugin {
    private String name;
    private String kongId;

    public void setKongId(String kongId) {
        this.kongId = kongId;
    }

    public String getKongId() {
        return kongId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}