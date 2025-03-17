package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@JmixEntity
@Table(name = "SITE_GATEWAY")
@Entity
public class SiteGateway {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME", nullable = false, length = 50)
    @NotNull
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JoinColumn(name = "SITE_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Site site;

    @JoinColumn(name = "GATEWAY_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private GatewayService gateway;

    @Column(name = "SITE_GATEWAY_ID", nullable = false, unique = true)
    @NotNull
    private String site_gateway_id;

    public String getSite_gateway_id() {
        return site_gateway_id;
    }

    public void setSite_gateway_id(String site_gateway_id) {
        this.site_gateway_id = site_gateway_id;
    }

    public GatewayService getGateway() {
        return gateway;
    }

    public void setGateway(GatewayService gateway) {
        this.gateway = gateway;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public Site getSite() {
        return site;
    }
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}