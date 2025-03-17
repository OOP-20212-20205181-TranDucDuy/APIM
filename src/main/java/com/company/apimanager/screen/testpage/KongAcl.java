package com.company.apimanager.screen.testpage;

import com.company.apimanager.app.KongAclConfigInfo;
import org.springframework.stereotype.Component;

@Component
public class KongAcl {
    private String name;
    private KongAclConfigInfo config;

    public KongAcl() {

    }
    public KongAcl(String name, KongAclConfigInfo kongAclConfigInfo) {
        this.name = name;
        this.config = kongAclConfigInfo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public KongAclConfigInfo getConfig() {
        return config;
    }

    public void setConfig(KongAclConfigInfo config) {
        this.config = config;
    }
}