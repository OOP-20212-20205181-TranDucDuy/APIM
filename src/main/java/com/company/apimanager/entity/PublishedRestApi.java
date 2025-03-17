package com.company.apimanager.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.UUID;

@JmixEntity
@Table(name = "PUBLISHED_REST_API", indexes = {
        @Index(name = "IDX_PUBLISHED_REST_API_PRODUCT", columnList = "PRODUCT_ID"),
        @Index(name = "IDX_PUBLISHED_REST_API_API", columnList = "API_ID")
})
@Entity
public class PublishedRestApi {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "PATH")
    private String path;

    @Column(name = "HOST")
    private String host;

    @Column(name = "KONG_SERVICE_ID")
    private UUID kongServiceId;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "PRODUCT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private PublishedProduct product;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @JoinColumn(name = "API_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private RestApi api;

    @Column(name = "PLUGIN")
    @Lob
    private String plugin;

    @Column(name = "ROUTE")
    @Lob
    private String route;

    @Column(name = "CONSENT_URL")
    private String consent_url;

    @Column(name = "OAUTH_PROVISION_KEY")
    private String oauthProvisionKey;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOauthProvisionKey() {
        return oauthProvisionKey;
    }

    public void setOauthProvisionKey(String oauthProvisionKey) {
        this.oauthProvisionKey = oauthProvisionKey;
    }

    public String getConsent_url() {
        return consent_url;
    }

    public void setConsent_url(String consent_url) {
        this.consent_url = consent_url;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public RestApi getApi() {
        return api;
    }

    public void setApi(RestApi api) {
        this.api = api;
    }

    public PublishedProduct getProduct() {
        return product;
    }

    public void setProduct(PublishedProduct product) {
        this.product = product;
    }

    public UUID getKongServiceId() {
        return kongServiceId;
    }

    public void setKongServiceId(UUID kongServiceId) {
        this.kongServiceId = kongServiceId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}