package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongTargetDetails {
    private String id;
    private String name;
    private Long weight;

    public KongTargetDetails() {

    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public Long getWeight() {
        return weight;
    }
}