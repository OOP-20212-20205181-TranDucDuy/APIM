package com.company.apimanager.app.kong_object;

import com.company.apimanager.app.kong_object.helper.BaseService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IpRestrictionSecurityPlugin {

    private ArrayList<String> protocols;
    private IpRestrictionSecurityPluginConfig config;
    private String name;

    @JsonProperty("service")
    private BaseService service;
    public IpRestrictionSecurityPlugin() {
    }
    public IpRestrictionSecurityPlugin(IpRestrictionSecurityPluginConfig config) {
        this.config = config;
        this.name = "ip-restriction";
        this.protocols = new ArrayList<>(Arrays.asList("grpc", "grpcs", "http", "https"));
    }
    public IpRestrictionSecurityPlugin(IpRestrictionSecurityPluginConfig config, UUID serviceId) {
        this.protocols = new ArrayList<>(Arrays.asList("grpc", "grpcs", "http", "https"));
        this.service = new BaseService(serviceId);
        this.config = config;
        this.name = "ip-restriction";
    }

    public ArrayList<String> getProtocols() {
        return protocols;
    }

    public void setProtocols(ArrayList<String> protocols) {
        this.protocols = protocols;
    }

    public IpRestrictionSecurityPluginConfig getConfig() {
        return config;
    }

    public void setConfig(IpRestrictionSecurityPluginConfig config) {
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
