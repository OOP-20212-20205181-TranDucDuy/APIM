package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@JmixEntity
@Table(name = "GATEWAY_USAGE_PERMISSION")
@Entity
public class GatewayUsagePermission {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME", nullable = false)
    @NotNull
    private String name;

    @Column(name = "PROVIDER_GATEWAY_ID", nullable = false, unique = true)
    @NotNull
    private String provider_gateway_id;

    @NotNull
    @JoinColumn(name = "API_PROVIDER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User api_provider;

    @NotNull
    @JoinColumn(name = "GATEWAY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private GatewayService gateway;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public GatewayService getGateway() {
        return gateway;
    }

    public void setGateway(GatewayService gateway) {
        this.gateway = gateway;
    }

    public User getApi_provider() {
        return api_provider;
    }

    public void setApi_provider(User api_provider) {
        this.api_provider = api_provider;
    }

    public String getProvider_gateway_id() {
        return provider_gateway_id;
    }

    public void setProvider_gateway_id(String provider_gateway_id) {
        this.provider_gateway_id = provider_gateway_id;
    }
}