package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "VIRTUAL_AREA")
@Entity
public class VirtualArea {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME", nullable = false, length = 50)
    @NotNull
    private String name;

    @Column(name = "TITLE", nullable = false, length = 100)
    @NotNull
    private String title;

    @Composition
    @OneToMany(mappedBy = "virtualArea")
    private List<GatewayService> gateways;

    @Composition
    @OneToMany(mappedBy = "virtualArea")
    private List<PortalService> portals;

    @Composition
    @OneToMany(mappedBy = "virtualArea")
    private List<AnalyticsService> analytics;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "IS_DEFAULT")
    private Boolean is_default;

    @Column(name = "IS_ACTIVE")
    private Boolean is_active;

    public List<AnalyticsService> getAnalytics() {
        return analytics;
    }

    public void setAnalytics(List<AnalyticsService> analytics) {
        this.analytics = analytics;
    }

    public List<PortalService> getPortals() {
        return portals;
    }

    public void setPortals(List<PortalService> portals) {
        this.portals = portals;
    }

    public List<GatewayService> getGateways() {
        return gateways;
    }

    public void setGateways(List<GatewayService> gateways) {
        this.gateways = gateways;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIs_default() { return is_default; }

    public void setIs_default(Boolean is_default) { this.is_default = is_default; }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Boolean getIs_active() {
        return is_active;
    }

    public void setIs_active(Boolean is_active) {
        this.is_active = is_active;
    }
}