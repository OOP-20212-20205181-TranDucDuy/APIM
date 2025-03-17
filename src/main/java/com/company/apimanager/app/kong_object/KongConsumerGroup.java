package com.company.apimanager.app.kong_object;


import org.springframework.stereotype.Component;

import java.sql.Timestamp;


@Component
public class KongConsumerGroup {
    private String id;
    private String name;

    private Timestamp created_at;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public KongConsumerGroup(){ }

    public KongConsumerGroup(String name){
        this.name = name;
    }


    public KongConsumerGroup(String id, String name, Timestamp created_at){
        this.id = id;
        this.name = name;
        this.created_at = created_at;
    }

}
