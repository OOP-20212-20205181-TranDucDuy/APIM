package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@JmixEntity
@Table(name = "PORTAL_USAGE_PERMISSION")
@Entity
public class PortalUsagePermission {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME", nullable = false)
    @NotNull
    private String name;

    @Column(name = "PROVIDER_PORTAL_ID", nullable = false, unique = true)
    @NotNull
    private String provider_portal_id;

    @NotNull
    @JoinColumn(name = "API_PROVIDER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User api_provider;

    @NotNull
    @JoinColumn(name = "PORTAL_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PortalService portal;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setApi_provider(User api_provider) {
        this.api_provider = api_provider;
    }

    public User getApi_provider() {
        return api_provider;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PortalService getPortal() {
        return portal;
    }

    public void setPortal(PortalService portal) {
        this.portal = portal;
    }

    public String getProvider_portal_id() {
        return provider_portal_id;
    }

    public void setProvider_portal_id(String provider_portal_id) {
        this.provider_portal_id = provider_portal_id;
    }
}