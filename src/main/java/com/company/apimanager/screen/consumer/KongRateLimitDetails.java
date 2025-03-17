package com.company.apimanager.screen.consumer;

import org.springframework.stereotype.Component;

@Component
public class KongRateLimitDetails {
    private String id;

    public KongRateLimitDetails () {

    }
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}