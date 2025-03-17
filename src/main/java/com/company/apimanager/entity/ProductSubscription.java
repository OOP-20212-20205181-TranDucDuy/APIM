package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@JmixEntity
@Table(name = "PRODUCT_SUBSCRIPTION", indexes = {
        @Index(name = "IDX_PRODUCTSUBSCRIPTION", columnList = "CONSUMER_ID"),
        @Index(name = "IDX_PRODUCTSUBSCRIPTION", columnList = "PUBLISHED_PRODUCT_ID")
})
@Entity
public class ProductSubscription {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "PRODUCT_CONSUMER_ID", nullable = false, unique = true)
    @NotNull
    private String product_consumer_id;

    @JoinColumn(name = "CONSUMER_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Consumer consumer;

    @JoinColumn(name = "PUBLISHED_PRODUCT_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PublishedProduct published_product;

    @Column(name = "SUBCRIPTION_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date subcription_date;

    @Column(name = "KONG_CONSUMER_GROUP_ID", nullable = false, unique = true)
    @NotNull
    private String kong_consumer_group_id;

    public String getProduct_consumer_id() {
        return product_consumer_id;
    }

    public void setProduct_consumer_id(String product_consumer_id) {
        this.product_consumer_id = product_consumer_id;
    }

    public Date getSubcription_date() {
        return subcription_date;
    }

    public void setSubcription_date(Date subcription_date) {
        this.subcription_date = subcription_date;
    }

    public PublishedProduct getPublished_product() {
        return published_product;
    }

    public void setPublished_product(PublishedProduct published_product) {
        this.published_product = published_product;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getKong_consumer_group_id() {
        return kong_consumer_group_id;
    }

    public void setKong_consumer_group_id(String kong_consumer_group_id) {
        this.kong_consumer_group_id = kong_consumer_group_id;
    }
}