package com.company.apimanager.app.kong_object.proxycache;

import java.util.ArrayList;

public class ProxyCachePluginConfig {
    public Integer storage_ttl;
    public ArrayList<Integer> response_code;
    public int cache_ttl;
    public ArrayList<String> vary_query_params;
    public ArrayList<String> request_method;
    public ArrayList<String> vary_headers;
    public boolean cache_control;
    public Memory memory;
    public String strategy;
    public ArrayList<String> content_type;

    public ProxyCachePluginConfig() {
    }

}
