package com.company.apimanager.app.kong_object.cors;

import java.util.ArrayList;
import java.util.Arrays;


public class CorsPluginConfig {

    public ArrayList<String> origins;
    public Integer max_age;
    public Boolean credentials;
    public ArrayList<String> methods;
    public Boolean preflight_continue;
    public ArrayList<String> exposed_headers;
    public ArrayList<String> headers;

    public CorsPluginConfig() {
    }

    public CorsPluginConfig(ArrayList<String> origins, ArrayList<String> exposed_headers, ArrayList<String> headers, Integer max_age, Boolean credentials) {
        this.origins = origins;
        this.exposed_headers = exposed_headers;
        this.headers = headers;
        this.max_age = max_age;
        this.credentials = credentials;
        this.methods = new ArrayList<>(Arrays.asList("GET",
                "HEAD",
                "PUT",
                "PATCH",
                "POST",
                "DELETE",
                "OPTIONS",
                "TRACE",
                "CONNECT"));
        this.preflight_continue = false;
    }

    public CorsPluginConfig(ArrayList<String> origins, ArrayList<String> exposed_headers, ArrayList<String> headers) {
        this.origins = origins;
        this.exposed_headers = exposed_headers;
        this.headers = headers;
        this.max_age = null;
        this.credentials = false;
        this.methods = new ArrayList<>(Arrays.asList("GET",
                "HEAD",
                "PUT",
                "PATCH",
                "POST",
                "DELETE",
                "OPTIONS",
                "TRACE",
                "CONNECT"));
        this.preflight_continue = false;
    }

    public ArrayList<String> getOrigins() {
        return origins;
    }

    public void setOrigins(ArrayList<String> origins) {
        this.origins = origins;
    }

    public Integer getMax_age() {
        return max_age;
    }

    public void setMax_age(Integer max_age) {
        this.max_age = max_age;
    }

    public Boolean getCredentials() {
        return credentials;
    }

    public void setCredentials(Boolean credentials) {
        this.credentials = credentials;
    }

    public ArrayList<String> getMethods() {
        return methods;
    }

    public void setMethods(ArrayList<String> methods) {
        this.methods = methods;
    }

    public Boolean getPreflight_continue() {
        return preflight_continue;
    }

    public void setPreflight_continue(Boolean preflight_continue) {
        this.preflight_continue = preflight_continue;
    }

    public ArrayList<String> getExposed_headers() {
        return exposed_headers;
    }

    public void setExposed_headers(ArrayList<String> exposed_headers) {
        this.exposed_headers = exposed_headers;
    }

    public ArrayList<String> getHeaders() {
        return headers;
    }

    public void setHeaders(ArrayList<String> headers) {
        this.headers = headers;
    }
}
