package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongServicePlugin {
    private String kongId;
    private String pluginName;
    private String oauthProvisionKey;
    private String[] oauthScopes;

    public KongServicePlugin() {

    }

    public String getKongId() {
        return kongId;
    }

    public void setKongId(String kongId) {
        this.kongId = kongId;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getOauthProvisionKey() {
        return oauthProvisionKey;
    }

    public String[] getOauthScopes() {
        return oauthScopes;
    }

    public void setOauthProvisionKey(String oauthProvisionKey) {
        this.oauthProvisionKey = oauthProvisionKey;
    }

    public void setOauthScopes(String[] oauthScopes) {
        this.oauthScopes = oauthScopes;
    }
}