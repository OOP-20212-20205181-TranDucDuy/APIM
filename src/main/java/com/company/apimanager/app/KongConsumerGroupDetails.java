package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongConsumerGroupDetails {
    private String id;
    private String consumerId;
    private String groupName;

    public KongConsumerGroupDetails () {

    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}