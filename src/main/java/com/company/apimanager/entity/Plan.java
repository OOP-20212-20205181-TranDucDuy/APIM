package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@JmixEntity
@Table(name = "PLAN_", indexes = {
        @Index(name = "IDX_PLAN_PRODUCT_ID", columnList = "PRODUCT_ID")
})
@Entity(name = "Plan_")
public class Plan {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull
    @InstanceName
    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "PERSECOND")
    private Long persecond;

    @Column(name = "PERMINUTE")
    private Long perminute;

    @Column(name = "PERHOUR")
    private Long perhour;

    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPerhour() {
        return perhour;
    }

    public void setPerhour(Long perhour) {
        this.perhour = perhour;
    }

    public Long getPerminute() {
        return perminute;
    }

    public void setPerminute(Long perminute) {
        this.perminute = perminute;
    }

    public Long getPersecond() {
        return persecond;
    }

    public void setPersecond(Long persecond) {
        this.persecond = persecond;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}