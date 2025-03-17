package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@JmixEntity
public class ApiCall {

    /*
    @JmixGeneratedValue
    @JmixId
    private UUID id;

     */

    @JmixProperty(mandatory = true)
    @NotNull
    private String method;

    private String endpoint;

    private String authorization_type;

    private String request_body;

    private String basic_authen_user;

    private String basic_authen_password;

    private String key_authen_name;

    private String key_authen_value;

    private String bearer_authen_token;

    @Composition
    private List<ApiHttpHeader> http_headers;

    public List<ApiHttpHeader> getHttp_headers() {
        return http_headers;
    }

    public void setHttp_headers(List<ApiHttpHeader> http_headers) {
        this.http_headers = http_headers;
    }

    public String getBearer_authen_token() {
        return bearer_authen_token;
    }

    public void setBearer_authen_token(String bearer_authen_token) {
        this.bearer_authen_token = bearer_authen_token;
    }

    public String getKey_authen_value() {
        return key_authen_value;
    }

    public void setKey_authen_value(String key_authen_value) {
        this.key_authen_value = key_authen_value;
    }

    public String getKey_authen_name() {
        return key_authen_name;
    }

    public void setKey_authen_name(String key_authen_name) {
        this.key_authen_name = key_authen_name;
    }

    public String getBasic_authen_password() {
        return basic_authen_password;
    }

    public void setBasic_authen_password(String basic_authen_password) {
        this.basic_authen_password = basic_authen_password;
    }

    public String getBasic_authen_user() {
        return basic_authen_user;
    }

    public void setBasic_authen_user(String basic_authen_user) {
        this.basic_authen_user = basic_authen_user;
    }

    public String getRequest_body() {
        return request_body;
    }

    public void setRequest_body(String request_body) {
        this.request_body = request_body;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setAuthorization_type(String authorization_type) {
        this.authorization_type = authorization_type;
    }

    public String getAuthorization_type() {
        return authorization_type;
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