package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@JmixEntity
public class DtoRestApi {

    /*
    @JmixGeneratedValue
    @JmixId
    private UUID id;
     */

    @InstanceName
    @JmixProperty(mandatory = true)
    @NotNull
    private String name;

    private UUID jpa_id;

    private String title;

    @JmixProperty(mandatory = true)
    @NotNull
    private String base_path;

    @JmixProperty(mandatory = true)
    @NotNull
    private String target_endpoint;

    @JmixProperty(mandatory = true)
    @NotNull
    private String security_method;

    private String importError;

    public String getSecurity_method() {
        return security_method;
    }

    public void setSecurity_method(String security_method) {
        this.security_method = security_method;
    }

    public String getTarget_endpoint() {
        return target_endpoint;
    }

    public void setTarget_endpoint(String target_endpoint) {
        this.target_endpoint = target_endpoint;
    }

    public String getBase_path() {
        return base_path;
    }

    public void setBase_path(String base_path) {
        this.base_path = base_path;
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

    public UUID getJpa_id() {
        return jpa_id;
    }

    public void setJpa_id(UUID jpa_id) {
        this.jpa_id = jpa_id;
    }

    public String getImportError() {
        return importError;
    }

    public void setImportError(String importError) {
        this.importError = importError;
    }
}