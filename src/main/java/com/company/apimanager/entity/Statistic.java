package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.util.UUID;

@JmixEntity
@Table(name = "STATISTIC")
@Entity
public class Statistic {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "DATE", nullable = false)
    @NotNull
    private String date;

    @Column(name = "TOTAL", nullable = false)
    @NotNull
    private Integer total;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public Integer getTotal() { return total; }

    public void setTotal(Integer total) { this.total = total; }
}