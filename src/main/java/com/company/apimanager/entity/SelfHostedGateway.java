package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@JmixEntity
@Table(name = "SELF_HOSTED_GATEWAY")
@Entity
public class SelfHostedGateway {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME", nullable = false, length = 50)
    @NotNull
    private String name;

    @Column(name = "MANAGEMENT_ENDPOINT", nullable = false)
    @NotNull
    private String management_endpoint;

    @Column(name = "INVOCATION_ENDPOINT")
    private String invocation_endpoint;

    @JoinColumn(name = "GATEWAY_TYPE_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private GatewayType gateway_type;

    @Column(name = "TITLE", length = 100)
    private String title;

    @Column(name = "TENANT_ID", length = 100)
    private String tenant_id;

    @Column(name = "TENANT_DESCRIPTION", length = 200)
    private String tenant_description;

    @Column(name = "TENANT_SUBNET", length = 100)
    private String tenant_subnet;

    @NotNull
    @JoinColumn(name = "OWNER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User owner;

    @Column(name = "OWNER_GATEWAYNAME", nullable = false, unique = true)
    @NotNull
    private String owner_gatewayname;

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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public GatewayType getGateway_type() {
        return gateway_type;
    }

    public String getInvocation_endpoint() {
        return invocation_endpoint;
    }

    public String getManagement_endpoint() {
        return management_endpoint;
    }

    public String getTenant_description() {
        return tenant_description;
    }

    public String getTenant_id() {
        return tenant_id;
    }

    public void setGateway_type(GatewayType gateway_type) {
        this.gateway_type = gateway_type;
    }

    public String getTenant_subnet() {
        return tenant_subnet;
    }

    public void setInvocation_endpoint(String invocation_endpoint) {
        this.invocation_endpoint = invocation_endpoint;
    }

    public void setManagement_endpoint(String management_endpoint) {
        this.management_endpoint = management_endpoint;
    }

    public void setTenant_description(String tenant_description) {
        this.tenant_description = tenant_description;
    }

    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }

    public void setTenant_subnet(String tenant_subnet) {
        this.tenant_subnet = tenant_subnet;
    }

    public String getOwner_gatewayname() {
        return owner_gatewayname;
    }

    public void setOwner_gatewayname(String owner_gatewayname) {
        this.owner_gatewayname = owner_gatewayname;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}