package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongConsumerDetails {
    private String userName;
    private String id;

    public  KongConsumerDetails() {

    }
    public  KongConsumerDetails(String userName) {
        this.userName = userName;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}