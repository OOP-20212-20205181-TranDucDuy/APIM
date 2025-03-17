package com.company.apimanager.app.kong_object;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;

@Component
@JsonDeserialize(as= RequestTransformerTransformationsPlugin.class)
public class RequestTransformerTransformationsPlugin {
//    public KongService service;
//    public Object consumer;
    public ArrayList<String> protocols;
    public RequestTransformerTransformationsPluginConfig config;
//    public int created_at;
    private String name;
//    public Object tags;
//    public boolean enabled;
//    public Object route;

//    public String id;

    public RequestTransformerTransformationsPlugin() {
    }

    public RequestTransformerTransformationsPlugin(RequestTransformerTransformationsPluginConfig config) {
        this.config = config;
        this.name = "request-transformer";
        ArrayList<String> protocolList = new ArrayList<>(Arrays.asList("grpc", "grpcs", "http", "https"));
        this.protocols = protocolList;
//        this.created_at = None;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getProtocols() {
        return protocols;
    }

    public void setProtocols(ArrayList<String> protocols) {
        this.protocols = protocols;
    }

    public RequestTransformerTransformationsPluginConfig getConfig() {
        return config;
    }

    public void setConfig(RequestTransformerTransformationsPluginConfig config) {
        this.config = config;
    }

}
