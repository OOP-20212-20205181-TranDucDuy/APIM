package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Entity;
import java.util.UUID;

@JmixEntity
public class ApiInSite {
    @JmixGeneratedValue
    @JmixId
    private UUID id;

    private String restApiId;
    private String api_id;
    private String route_id;
    private String acl_id;
    private String name;
    private String host;
    private String path;
    private String site_id;
    private String published_product_id;
    private String api_url;
    private String consent_url;
    private String oauthProvisionKey;

    public String getRestApiId() {
        return restApiId;
    }

    public void setRestApiId(String restApiId) {
        this.restApiId = restApiId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRoute_id() {
        return route_id;
    }

    public void setRoute_id(String route_id) {
        this.route_id = route_id;
    }

    public String getAcl_id() {
        return acl_id;
    }

    public void setAcl_id(String acl_id) {
        this.acl_id = acl_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public String getApi_id() {
        return api_id;
    }

    public void setApi_id(String api_id) {
        this.api_id = api_id;
    }

    public String getSite_id() {
        return site_id;
    }

    public void setSite_id(String site_id) {
        this.site_id = site_id;
    }

    public void setApi_url(String api_url) {
        this.api_url = api_url;
    }

    public String getApi_url() {
        return api_url;
    }

    public String getPublished_product_id() {
        return published_product_id;
    }

    public void setPublished_product_id(String published_product_id) {
        this.published_product_id = published_product_id;
    }

    public String getConsent_url() {
        return consent_url;
    }

    public void setConsent_url(String consent_url) {
        this.consent_url = consent_url;
    }

    public void setOauthProvisionKey(String oauthProvisionKey) {
        this.oauthProvisionKey = oauthProvisionKey;
    }

    public String getOauthProvisionKey() {
        return oauthProvisionKey;
    }
}