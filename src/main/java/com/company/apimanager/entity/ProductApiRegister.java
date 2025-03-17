package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@JmixEntity
@Table(name = "PRODUCT_API_REGISTER", indexes = {
        @Index(name = "IDX_PRODUCTAPIREGISTER", columnList = "PRODUCT_ID"),
        @Index(name = "IDX_PRODUCTAPIREGISTER", columnList = "REST_API_ID")
})
@Entity
public class ProductApiRegister {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

    @JoinColumn(name = "REST_API_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RestApi rest_api;

    @Temporal(TemporalType.DATE)
    @Column(name = "REGISTER_DATETIME")
    private Date register_datetime;

    @NotNull
    @Column(name = "PRODUCT_API", nullable = false, unique = true)
    private String product_api;

    public void setRegister_datetime(Date register_datetime) {
        this.register_datetime = register_datetime;
    }

    public Date getRegister_datetime() {
        return register_datetime;
    }

    public String getProduct_api() {
        return product_api;
    }

    public void setProduct_api(String product_api) {
        this.product_api = product_api;
    }

    public RestApi getRest_api() {
        return rest_api;
    }

    public void setRest_api(RestApi rest_api) {
        this.rest_api = rest_api;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}