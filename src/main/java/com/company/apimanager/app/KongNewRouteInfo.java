package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongNewRouteInfo {
    private String serviceName;
    private String name;
    private String path;

    public KongNewRouteInfo() {

    }
    public KongNewRouteInfo(String serviceName, String name, String path) {
        this.serviceName = serviceName;
        this.name = name;
        this.path = path;
    }
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}