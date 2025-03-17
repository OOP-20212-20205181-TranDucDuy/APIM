package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.UUID;

@JmixEntity
@Table(name = "DRAFT_PLAN", indexes = {
        @Index(name = "IDX_DRAFTPLAN", columnList = "DRAFT_PRODUCT_ID")
})
@Entity
public class DraftPlan {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "RATELIMIT")
    private Integer ratelimit;
    @JoinColumn(name = "DRAFT_PRODUCT_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private DraftProduct draftProduct;

    public DraftProduct getDraftProduct() {
        return draftProduct;
    }

    public void setDraftProduct(DraftProduct draftProduct) {
        this.draftProduct = draftProduct;
    }

    public void setRatelimit(Integer ratelimit) {
        this.ratelimit = ratelimit;
    }

    public Integer getRatelimit() {
        return ratelimit;
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