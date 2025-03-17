package com.company.apimanager.app.kong_object;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConsumerJwtCredential {
    private String rsa_public_key;
    private String secret;
    private Object tags;
    private String algorithm;
    private String key;

    public ConsumerJwtCredential(String rsa_public_key, String secret, Object tags, String algorithm, String id, String key) {
        this.rsa_public_key = rsa_public_key;
        this.secret = secret;
        this.tags = tags;
        this.algorithm = algorithm;
        this.key = key;
    }

    @Autowired
    public ConsumerJwtCredential() {
        this.rsa_public_key = null;
        this.secret = null;
        this.tags = null;
        this.algorithm = "HS256";
        this.key = null;
    }

    public String getRsa_public_key() {
        return rsa_public_key;
    }

    public void setRsa_public_key(String rsa_public_key) {
        this.rsa_public_key = rsa_public_key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Object getTags() {
        return tags;
    }

    public void setTags(Object tags) {
        this.tags = tags;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

