package com.company.apimanager.app.kong_object;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;

@Component
public class JwtAuthenticationPlugin {
    private String id;
    private String name;
    private JwtAuthenticationPluginConfig config;
    private Object route;
    private Object tags;
    private boolean enabled;
    private ArrayList<String> protocols;
//    private Object consumer;
//    private int created_at;


    public JwtAuthenticationPlugin(String id, String name, JwtAuthenticationPluginConfig config, Object route, Object tags, boolean enabled, ArrayList<String> protocols) {
        this.id = id;
        this.name = name;
        this.config = config;
        this.route = route;
        this.tags = tags;
        this.enabled = enabled;
        this.protocols = protocols;
    }
    @Autowired
    public JwtAuthenticationPlugin(JwtAuthenticationPluginConfig config) {
        this.id = null;
        this.name = "jwt";
        this.config = config;
        this.route = null;
        this.tags = null;
        this.enabled = true;
        this.protocols = new ArrayList<>(Arrays.asList("grpc", "grpcs", "http", "https"));
    }

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

    public JwtAuthenticationPluginConfig getConfig() {
        return config;
    }

    public void setConfig(JwtAuthenticationPluginConfig config) {
        this.config = config;
    }

    public Object getRoute() {
        return route;
    }

    public void setRoute(Object route) {
        this.route = route;
    }

    public Object getTags() {
        return tags;
    }

    public void setTags(Object tags) {
        this.tags = tags;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ArrayList<String> getProtocols() {
        return protocols;
    }

    public void setProtocols(ArrayList<String> protocols) {
        this.protocols = protocols;
    }

}
