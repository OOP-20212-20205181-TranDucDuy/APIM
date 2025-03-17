package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@JmixEntity
@Table(name = "PRICING_PLAN", indexes = {
        @Index(name = "IDX_PRICING_PLAN_UNQ_NAME", columnList = "NAME", unique = true)
})
@Entity
public class PricingPlan {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME", nullable = false)
    @NotNull
    private String name;

    @Column(name = "RATE_LIMIT", nullable = false)
    @NotNull
    private Integer rate_limit;

    @Column(name = "DEVELOPER_PORTAL", nullable = false)
    @NotNull
    private Boolean developer_portal = false;

    @Column(name = "CUSTOM_DOMAIN", nullable = false)
    @NotNull
    private Boolean custom_domain = false;

    @Column(name = "SELF_HOSTED_GATEWAY", nullable = false)
    @NotNull
    private Boolean self_hosted_gateway = false;

    @Column(name = "MESSAGE_SIZE", nullable = false)
    @NotNull
    private Long message_size;

    @Column(name = "LOGING_RETENTION", nullable = false)
    @NotNull
    private Integer loging_retention;

    @NotNull
    @Column(name = "SLA", nullable = false)
    private Double sla;

    @NotNull
    @Column(name = "PRICE", nullable = false)
    private Long price;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;

    public void setPrice(Long price) {
        this.price = price;
    }

    public Long getPrice() {
        return price;
    }

    public void setSla(Double sla) {
        this.sla = sla;
    }

    public Double getSla() {
        return sla;
    }

    public Integer getLoging_retention() {
        return loging_retention;
    }

    public void setLoging_retention(Integer loging_retention) {
        this.loging_retention = loging_retention;
    }

    public Long getMessage_size() {
        return message_size;
    }

    public void setMessage_size(Long message_size) {
        this.message_size = message_size;
    }

    public Boolean getSelf_hosted_gateway() {
        return self_hosted_gateway;
    }

    public void setSelf_hosted_gateway(Boolean self_hosted_gateway) {
        this.self_hosted_gateway = self_hosted_gateway;
    }

    public Boolean getCustom_domain() {
        return custom_domain;
    }

    public void setCustom_domain(Boolean custom_domain) {
        this.custom_domain = custom_domain;
    }

    public Boolean getDeveloper_portal() {
        return developer_portal;
    }

    public void setDeveloper_portal(Boolean developer_portal) {
        this.developer_portal = developer_portal;
    }

    public Integer getRate_limit() {
        return rate_limit;
    }

    public void setRate_limit(Integer rate_limit) {
        this.rate_limit = rate_limit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}