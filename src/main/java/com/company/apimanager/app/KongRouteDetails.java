package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongRouteDetails {
    private String id;
    private String hosts;
    private Boolean request_buffering;
    private Boolean response_buffering;
    private String name;
    //private String tags;
    private int https_redirect_status_code;
    private Boolean preserve_host;
    private int regex_priority;
    private long updated_at;
    private long created_at;

    private String[] paths;

    public KongRouteDetails() {

    }

    public void Assign(KongRouteDetails other) {
        this.id = other.id;
        this.hosts = other.hosts;
        this.request_buffering = other.request_buffering;
        this.response_buffering = other.response_buffering;
        this.name = other.name;
        this.https_redirect_status_code = other.https_redirect_status_code;
        this.preserve_host = other.preserve_host;
        this.regex_priority = other.regex_priority;
        this.updated_at = other.updated_at;
        this.created_at = other.created_at;
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

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }

    public Boolean getRequest_buffering() {
        return request_buffering;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public Boolean getPreserve_host() {
        return preserve_host;
    }

    public Boolean getResponse_buffering() {
        return response_buffering;
    }

    public int getHttps_redirect_status_code() {
        return https_redirect_status_code;
    }

    public String getHosts() {
        return hosts;
    }

    public long getCreated_at() {
        return created_at;
    }

    public int getRegex_priority() {
        return regex_priority;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    public void setHttps_redirect_status_code(int https_redirect_status_code) {
        this.https_redirect_status_code = https_redirect_status_code;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public void setPreserve_host(Boolean preserve_host) {
        this.preserve_host = preserve_host;
    }

    public void setRegex_priority(int regex_priority) {
        this.regex_priority = regex_priority;
    }

    public void setRequest_buffering(Boolean request_buffering) {
        this.request_buffering = request_buffering;
    }

    public void setResponse_buffering(Boolean response_buffering) {
        this.response_buffering = response_buffering;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }

    public String[] getPaths() {
        return paths;
    }
}