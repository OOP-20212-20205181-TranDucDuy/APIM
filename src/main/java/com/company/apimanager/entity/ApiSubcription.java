package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@JmixEntity
@Table(name = "API_SUBCRIPTION", indexes = {
        @Index(name = "IDX_APISUBCRIPTION", columnList = "CONSUMER_ID"),
        @Index(name = "IDX_APISUBCRIPTION", columnList = "SUBCRIBED_API_ID")
})
@Entity
public class ApiSubcription {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @JoinColumn(name = "CONSUMER_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Consumer consumer;

    @JoinColumn(name = "SUBCRIBED_API_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RestApi subcribed_api;

    @Column(name = "SUBCRIBED_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date subcribed_date;

    public Date getSubcribed_date() {
        return subcribed_date;
    }

    public void setSubcribed_date(Date subcribed_date) {
        this.subcribed_date = subcribed_date;
    }

    public RestApi getSubcribed_api() {
        return subcribed_api;
    }

    public void setSubcribed_api(RestApi subcribed_api) {
        this.subcribed_api = subcribed_api;
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
}