package com.company.apimanager.app.kong_object;


import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;

@Component
public class ResponseTransformerTransformationsPlugin {
//    public KongService service;
//    public Object consumer;
    public ArrayList<String> protocols;
    public ResponseTransformerTransformationsPluginConfig config;
//    public int created_at;
    public String name;
//    public Object tags;
//    public boolean enabled;
//    public Object route;
//    public String id;

    public ResponseTransformerTransformationsPlugin() {
    }


    public ResponseTransformerTransformationsPlugin(ResponseTransformerTransformationsPluginConfig config) {
        this.config = config;
        ArrayList<String> protocolList = new ArrayList<>(Arrays.asList("grpc", "grpcs", "http", "https"));
        this.protocols = protocolList;
        this.name = "response-transformer";
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

    public ResponseTransformerTransformationsPluginConfig getConfig() {
        return config;
    }

    public void setConfig(ResponseTransformerTransformationsPluginConfig config) {
        this.config = config;
    }

}
