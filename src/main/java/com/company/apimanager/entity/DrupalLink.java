package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@JmixEntity
@Table(name = "DRUPAL_LINK")
@Entity
public class DrupalLink {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @JoinColumn(name = "PUBLISHED_PRODUCT_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.REMOVE)
    private PublishedProduct publishedProduct;

    @JoinColumn(name = "REST_API_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RestApi restApi;

    @Column(name = "KONG_API_ID", nullable = true, length = 50)
    private String kong_api_id;

    @Column(name = "DRUPAL_API_ID", nullable = true, length = 50)
    private String drupal_api_id;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public PublishedProduct getPublishedProduct() { return publishedProduct; }

    public void setPublishedProduct(PublishedProduct publishedProduct) { this.publishedProduct = publishedProduct; }

    public RestApi getRestApi() { return restApi; }

    public void setRestApi(RestApi restApi) { this.restApi = restApi; }

    public String getDrupal_api_id() { return drupal_api_id; }

    public void setDrupal_api_id(String drupal_api_id) { this.drupal_api_id = drupal_api_id; }

    public String getKong_api_id() { return kong_api_id; }

    public void setKong_api_id(String kong_api_id) { this.kong_api_id = kong_api_id; }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}