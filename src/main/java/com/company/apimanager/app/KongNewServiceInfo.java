package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongNewServiceInfo {
    private String name;
    private String url;
    private String upstreamName;
    private String upstreamPath;

    public KongNewServiceInfo(){

    }
    public KongNewServiceInfo(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUpstreamName() {
        return upstreamName;
    }

    public String getUpstreamPath() {
        return upstreamPath;
    }

    public void setUpstreamName(String upstreamName) {
        this.upstreamName = upstreamName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUpstreamPath(String upstreamPath) {
        this.upstreamPath = upstreamPath;
    }
}