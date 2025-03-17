package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@JmixEntity
@Table(name = "BALANCED_HOST")
@Entity
public class BalancedHost {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME", nullable = false, length = 50)
    @NotNull
    private String name;

    @Column(name = "HOSTNAME", nullable = false, length = 100)
    @NotNull
    private String hostname;

    @Column(name = "HOST_PORT", nullable = false)
    @NotNull
    private Long host_port;

    @Column(name = "WEIGHT", nullable = false)
    @NotNull
    private Long weight;

    @JoinColumn(name = "REST_API_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RestApi restApi;

    @Column(name = "API_HOST", nullable = false, unique = true)
    @NotNull
    private String api_host;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getWeight() {
        return weight;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setHost_port(Long host_port) {
        this.host_port = host_port;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getHostname() {
        return hostname;
    }

    public Long getHost_port() {
        return host_port;
    }

    public RestApi getRestApi() {
        return restApi;
    }

    public void setRestApi(RestApi restApi) {
        this.restApi = restApi;
    }

    public void setApi_host(String api_host) {
        this.api_host = api_host;
    }

    public String getApi_host() {
        return api_host;
    }
}