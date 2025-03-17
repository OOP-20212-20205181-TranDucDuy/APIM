package com.company.apimanager.app.kong_object.cors;

import com.company.apimanager.app.kong_object.helper.BaseService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CorsPlugin {
    @JsonIgnore
    private static String PLUGIN_NAME = "cors";
    private ArrayList<String> protocols;
    private CorsPluginConfig config;
    private String name;

    public boolean enabled;

    @JsonProperty("service")
    private BaseService service;
    public CorsPlugin() {
    }
    public CorsPlugin(CorsPluginConfig config) {
        this.config = config;
        this.name = PLUGIN_NAME;
        this.protocols = new ArrayList<>(Arrays.asList("grpc", "grpcs", "http", "https"));
        this.enabled = true;
    }
    public CorsPlugin(CorsPluginConfig config, UUID serviceId) {
        this.protocols = new ArrayList<>(Arrays.asList("grpc", "grpcs", "http", "https"));
        this.service = new BaseService(serviceId);
        this.config = config;
        this.name = PLUGIN_NAME;
        this.enabled = true;
    }

    public ArrayList<String> getProtocols() {
        return protocols;
    }

    public void setProtocols(ArrayList<String> protocols) {
        this.protocols = protocols;
    }

    public CorsPluginConfig getConfig() {
        return config;
    }

    public void setConfig(CorsPluginConfig config) {
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public BaseService getService() {
        return service;
    }

    public void setService(BaseService service) {
        this.service = service;
    }
}
