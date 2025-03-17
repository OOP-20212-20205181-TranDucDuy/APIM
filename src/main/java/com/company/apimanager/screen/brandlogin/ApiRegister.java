package com.company.apimanager.screen.brandlogin;

import com.company.apimanager.entity.Product;
import com.company.apimanager.entity.RestApi;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@JmixEntity
@Table(name = "API_REGISTER", indexes = {
        @Index(name = "IDX_APIREGISTER_API_ID", columnList = "API_ID"),
        @Index(name = "IDX_APIREGISTER_PRODUCT_ID", columnList = "PRODUCT_ID")
})
@Entity
public class ApiRegister {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @JoinColumn(name = "API_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RestApi api;

    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

    @Temporal(TemporalType.DATE)
    @NotNull
    @Column(name = "REGISTER_DATETIME", nullable = false)
    private Date register_datetime;

    @Column(name = "API_PRODUCT_ID", nullable = false, unique = true)
    @NotNull
    private String api_product_id;

    @Column(name = "DRUPAL_API_ID")
    private Integer drupal_api_id;

    public void setRegister_datetime(Date register_datetime) {
        this.register_datetime = register_datetime;
    }

    public Date getRegister_datetime() {
        return register_datetime;
    }

    public String getApi_product_id() {
        return api_product_id;
    }

    public void setApi_product_id(String api_product_id) {
        this.api_product_id = api_product_id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public RestApi getApi() {
        return api;
    }

    public void setApi(RestApi api) {
        this.api = api;
    }

    public Integer getDrupal_api_id() { return drupal_api_id; }

    public void setDrupal_api_id(Integer drupalApiId) { this.drupal_api_id = drupalApiId; }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}