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
@Table(name = "PRODUCT", indexes = {
        @Index(name = "IDX_PRODUCT", columnList = "VISIBILITY_TYPE_ID"),
        @Index(name = "IDX_PRODUCT_OWNER_ID", columnList = "OWNER_ID")
})
@Entity
public class Product {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME", nullable = false, length = 50)
    @NotNull
    private String name;

    @Column(name = "TITLE")
    private String title;

    @NotNull
    @JoinColumn(name = "VISIBILITY_TYPE_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private VisibilyType visibility_type;

    @JoinColumn(name = "OWNER_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User owner;

    @NotNull
    @Column(name = "OWNER_PRDNAME", nullable = false, unique = true)
    private String owner_prdname;

    @Composition
    @OneToMany(mappedBy = "product")
    private List<Plan> plans;

    public List<Plan> getPlans() {
        return plans;
    }

    public void setPlans(List<Plan> plans) {
        this.plans = plans;
    }

    public String getOwner_prdname() {
        return owner_prdname;
    }

    public void setOwner_prdname(String owner_prdname) {
        this.owner_prdname = owner_prdname;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public VisibilyType getVisibility_type() {
        return visibility_type;
    }

    public void setVisibility_type(VisibilyType visibility_type) {
        this.visibility_type = visibility_type;
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