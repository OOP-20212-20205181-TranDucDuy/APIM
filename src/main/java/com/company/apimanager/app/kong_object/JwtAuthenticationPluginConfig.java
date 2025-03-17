package com.company.apimanager.app.kong_object;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;

@Component
public class JwtAuthenticationPluginConfig {
    private boolean secret_is_base64;
    private String key_claim_name;
    private Object claims_to_verify;
    private int maximum_expiration;
    private ArrayList<String> header_names;
    private ArrayList<Object> cookie_names;
    private boolean run_on_preflight;
    private ArrayList<String> uri_param_names;
    private Object anonymous;


    public JwtAuthenticationPluginConfig(boolean secret_is_base64, String key_claim_name, Object claims_to_verify, int maximum_expiration, ArrayList<String> header_names, ArrayList<Object> cookie_names, boolean run_on_preflight, ArrayList<String> uri_param_names, Object anonymous) {
        this.secret_is_base64 = secret_is_base64;
        this.key_claim_name = key_claim_name;
        this.claims_to_verify = claims_to_verify;
        this.maximum_expiration = maximum_expiration;
        this.header_names = header_names;
        this.cookie_names = cookie_names;
        this.run_on_preflight = run_on_preflight;
        this.uri_param_names = uri_param_names;
        this.anonymous = anonymous;
    }
    @Autowired
    public JwtAuthenticationPluginConfig() {
        this.secret_is_base64 = false;
        this.key_claim_name = "iss";
        this.claims_to_verify = null;
        this.maximum_expiration = 0;
        ArrayList<String> header_namesList = new ArrayList<>(Arrays.asList("authorization"));
        this.header_names = header_namesList;
        ArrayList<Object> cookie_namesList = new ArrayList();
        this.cookie_names = cookie_namesList;
        this.run_on_preflight = true;
        ArrayList<String> uri_param_namesList = new ArrayList<>(Arrays.asList("jwt"));
        this.uri_param_names = uri_param_namesList;
        this.anonymous = null;
    }


    public boolean isSecret_is_base64() {
        return secret_is_base64;
    }

    public void setSecret_is_base64(boolean secret_is_base64) {
        this.secret_is_base64 = secret_is_base64;
    }

    public String getKey_claim_name() {
        return key_claim_name;
    }

    public void setKey_claim_name(String key_claim_name) {
        this.key_claim_name = key_claim_name;
    }

    public Object getClaims_to_verify() {
        return claims_to_verify;
    }

    public void setClaims_to_verify(Object claims_to_verify) {
        this.claims_to_verify = claims_to_verify;
    }

    public int getMaximum_expiration() {
        return maximum_expiration;
    }

    public void setMaximum_expiration(int maximum_expiration) {
        this.maximum_expiration = maximum_expiration;
    }

    public ArrayList<String> getHeader_names() {
        return header_names;
    }

    public void setHeader_names(ArrayList<String> header_names) {
        this.header_names = header_names;
    }

    public ArrayList<Object> getCookie_names() {
        return cookie_names;
    }

    public void setCookie_names(ArrayList<Object> cookie_names) {
        this.cookie_names = cookie_names;
    }

    public boolean isRun_on_preflight() {
        return run_on_preflight;
    }

    public void setRun_on_preflight(boolean run_on_preflight) {
        this.run_on_preflight = run_on_preflight;
    }

    public ArrayList<String> getUri_param_names() {
        return uri_param_names;
    }

    public void setUri_param_names(ArrayList<String> uri_param_names) {
        this.uri_param_names = uri_param_names;
    }

    public Object getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(Object anonymous) {
        this.anonymous = anonymous;
    }
}
