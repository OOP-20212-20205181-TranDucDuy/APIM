package com.company.apimanager.app.kong_object;

import com.company.apimanager.app.kong_object.helper.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;


public class IpRestrictionSecurityPluginConfig {

    private ArrayList<String> allow;
    private int status;
    private ArrayList<String> deny;
    private String message;

    public IpRestrictionSecurityPluginConfig() {
    }

    public IpRestrictionSecurityPluginConfig(ArrayList<String> allow, int status, ArrayList<String> deny, String message) {
        this.allow = allow;
        this.status = status;
        this.deny = deny;
        this.message = message;
    }

    public IpRestrictionSecurityPluginConfig(ArrayList<String> allow, ArrayList<String> deny) {
        this.allow = allow;
        this.deny = deny;
        this.status = 403;
    }

    public ArrayList<String> getAllow() {
        return allow;
    }

    public void setAllow(ArrayList<String> allow) {
        this.allow = allow;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ArrayList<String> getDeny() {
        return deny;
    }

    public void setDeny(ArrayList<String> deny) {
        this.deny = deny;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
