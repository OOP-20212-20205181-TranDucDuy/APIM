package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongOAuthClient {
    private String kongId;
    private String clientId;
    private String clientSecret;
    private String consumerId;
    private String clientName;
    private String clientRedirectUri;

    public KongOAuthClient() {

    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setClientRedirectUri(String clientRedirectUri) {
        this.clientRedirectUri = clientRedirectUri;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientRedirectUri() {
        return clientRedirectUri;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getKongId() {
        return kongId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setKongId(String kongId) {
        this.kongId = kongId;
    }
}