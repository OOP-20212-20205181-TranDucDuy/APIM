package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@JmixEntity
@Table(name = "PUBLISHED_PRODUCT", indexes = {
        @Index(name = "IDX_PUBLISHEDPRODUCT", columnList = "PRODUCT_ID"),
        @Index(name = "IDX_PUBLISHEDPRODUCT_SITE_ID", columnList = "SITE_ID")
})
@Entity
public class PublishedProduct {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

    @JoinColumn(name = "SITE_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Site site;

    @Column(name = "PUBLISHED_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date published_date;

    @Column(name = "STATE", nullable = false, length = 50)
    @NotNull
    private String state;

    @NotNull
    @Column(name = "PRODUCT_SITE_ID", nullable = false, unique = true)
    private String product_site_id;

    @Column(name = "KONG_OBJECT_IDS", length = 9000)
    private String kong_object_ids;

    @Column(name = "KONG_OAUTH_INFO", length = 9000)
    private String kong_oauth_info;

    @Column(name = "KONG_MAPPING_INFO", length = 9000)
    private String kong_mapping_info;

    @Column(name = "KONG_OBJECT_JSON", length = 10000)
    private String kong_object_json;

    @Column(name = "DRUPAL_PRODUCT_ID")
    private Integer drupal_product_id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProduct_site_id() {
        return product_site_id;
    }

    public void setProduct_site_id(String product_site_id) {
        this.product_site_id = product_site_id;
    }

    public Date getPublished_date() {
        return published_date;
    }

    public void setPublished_date(Date published_date) {
        this.published_date = published_date;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
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

    public String getKong_object_ids() {
        return kong_object_ids;
    }

    public void setKong_object_ids(String kong_object_ids) {
        this.kong_object_ids = kong_object_ids;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getKong_object_json() {
        return kong_object_json;
    }

    public void setKong_object_json(String kong_object_json) {
        this.kong_object_json = kong_object_json;
    }

    public String getKong_oauth_info() {
        return kong_oauth_info;
    }

    public String getKong_mapping_info() { return kong_mapping_info; }

    public void setKong_mapping_info(String kong_mapping_info) { this.kong_mapping_info = kong_mapping_info; }

    public void setKong_oauth_info(String kong_oauth_info) {
        this.kong_oauth_info = kong_oauth_info;
    }

    public Integer getDrupal_product_id() { return drupal_product_id; }

    public void setDrupal_product_id(Integer drupalProductId) { this.drupal_product_id = drupalProductId; }
}