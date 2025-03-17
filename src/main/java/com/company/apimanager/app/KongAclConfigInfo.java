package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongAclConfigInfo {
    private String[] allow;
    private boolean hide_group_header;

    public KongAclConfigInfo() {

    }
    public KongAclConfigInfo(String[] allow, boolean hide_group_header) {
        this.allow = allow;
        this.hide_group_header = hide_group_header;
    }
    public boolean gethide_group_header() {
        return hide_group_header;
    }

    public String[] getallow() {
        return allow;
    }

    public void setallow(String[] allow) {
        this.allow = allow;
    }

    public void sethide_group_header(boolean hide_group_header) {
        this.hide_group_header = hide_group_header;
    }
}