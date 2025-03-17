package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@JmixEntity
public class ApiHttpHeader {
    /*
    @JmixGeneratedValue
    @JmixId
    private UUID id;

     */

    @JmixProperty(mandatory = true)
    @NotNull
    private String header_name;

    @JmixProperty(mandatory = true)
    @NotNull
    private String header_value;

    public String getHeader_value() {
        return header_value;
    }

    public void setHeader_value(String header_value) {
        this.header_value = header_value;
    }

    public String getHeader_name() {
        return header_name;
    }

    public void setHeader_name(String header_name) {
        this.header_name = header_name;
    }
/*
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

 */
}