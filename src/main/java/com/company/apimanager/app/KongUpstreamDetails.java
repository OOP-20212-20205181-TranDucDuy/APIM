package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongUpstreamDetails {
    private String id;
    private String name;
    private String algorithm;

    public KongUpstreamDetails() {

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}