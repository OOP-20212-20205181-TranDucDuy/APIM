package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@JmixEntity
@Table(name = "ANALYTICS_SERVICE", indexes = {
        @Index(name = "IDX_ANALYTICSSERVICE", columnList = "VIRTUAL_AREA_ID")
})
@Entity
public class AnalyticsService {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME", nullable = false, unique = true, length = 50)
    @NotNull
    private String name;

    @Column(name = "TITLE", nullable = false)
    @NotNull
    private String title;

    @Column(name = "MANAGEMENT_ENDPOINT", nullable = false)
    @NotNull
    private String management_endpoint;

    @JoinColumn(name = "VIRTUAL_AREA_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private VirtualArea virtualArea;

    public VirtualArea getVirtualArea() {
        return virtualArea;
    }

    public void setVirtualArea(VirtualArea virtualArea) {
        this.virtualArea = virtualArea;
    }

    public String getManagement_endpoint() {
        return management_endpoint;
    }

    public void setManagement_endpoint(String management_endpoint) {
        this.management_endpoint = management_endpoint;
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}