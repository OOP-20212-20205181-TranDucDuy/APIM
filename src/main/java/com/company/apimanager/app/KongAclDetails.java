package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongAclDetails {
    private String id;
    private String[] groupList;

    public KongAclDetails() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setGroupList(String[] groupList) {
        this.groupList = groupList;
    }

    public String[] getGroupList() {
        return groupList;
    }
}