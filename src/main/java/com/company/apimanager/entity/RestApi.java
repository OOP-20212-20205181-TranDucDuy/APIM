package com.company.apimanager.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "REST_API", indexes = {
        @Index(name = "IDX_RESTAPI", columnList = "SECURITY_METHOD_ID"),
        @Index(name = "IDX_RESTAPI_OWNER_ID", columnList = "OWNER_ID")
})
@Entity
public class RestApi {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME", nullable = false, length = 50)
    @NotNull
    private String name;

    @Column(name = "TITLE")
    private String title;

    @NotNull
    @Column(name = "BASE_PATH", nullable = false)
    private String base_path;

    @Column(name = "TARGET_ENDPOINT", nullable = false)
    @NotNull
    private String target_endpoint;

    @Column(name = "HOST_GROUP_PATH")
    private String host_group_path;

    @JoinColumn(name = "SECURITY_METHOD_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ApiSecurityMethod security_method;

    @NotNull
    @JoinColumn(name = "OWNER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User owner;

    @Column(name = "OWNER_APINAME", nullable = false, unique = true)
    @NotNull
    private String owner_apiname;

    @Column(name = "PATH_IN_GW", nullable = false)
    @NotNull
    private String path_in_gw;

    @Column(name = "DOCUMENTATION")
    private String documentation;

    @Column(name = "ENABLE_DOC")
    private Boolean enable_doc;

    @Column(name = "UPSTREAM_HEADER_NAME")
    private String upstream_header_name;

    @Column(name = "UPSTREAM_HEADER_VALUE")
    private String upstream_header_value;

    @Column(name = "ENABLE_VALIDATE_SCHEMA")
    private Boolean enable_validation_schema;

    @Column(name = "VERBOSE_RESPONSE")
    private Boolean verbose_response;

    @Column(name = "BODY_SCHEMA")
    private String body_schema;

    @Column(name = "PARAMETER_SCHEMA")
    private String parameter_schema;

    @Column(name = "ALLOW_CONTENT_TYPE")
    private String allow_content_type;
    @Column(name = "REQUEST_TRANSFORMER", length = 5000)
    private String requestTransformer;

    @Column(name = "RESPONSE_TRANSFORMER", length = 5000)
    private String responseTransformer;

    @Column(name = "CORS", length = 5000)
    private String cors;

    @Composition
    @OneToMany(mappedBy = "restApi")
    private List<BalancedHost> balancedHosts;


    public String getOwner_apiname() {
        return owner_apiname;
    }

    public void setOwner_apiname(String owner_apiname) {
        this.owner_apiname = owner_apiname;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public ApiSecurityMethod getSecurity_method() {
        return security_method;
    }

    public void setSecurity_method(ApiSecurityMethod security_method) {
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPath_in_gw() {
        return path_in_gw;
    }

    public void setPath_in_gw(String path_in_gw) {
        this.path_in_gw = path_in_gw;
    }

    public String getDocumentation() { return documentation;}

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public Boolean getEnable_doc() { return enable_doc;}

    public void setEnable_doc(Boolean enable_doc) {
        this.enable_doc = enable_doc;
    }

    public String getUpstream_header_name() {
        return upstream_header_name;
    }

    public String getUpstream_header_value() {
        return upstream_header_value;
    }

    public void setUpstream_header_name(String upstream_header_name) {
        this.upstream_header_name = upstream_header_name;
    }

    public void setUpstream_header_value(String upstream_header_value) {
        this.upstream_header_value = upstream_header_value;
    }

    public String getHost_group_path() {
        return host_group_path;
    }

    public void setHost_group_path(String host_group_path) {
        this.host_group_path = host_group_path;
    }

    public List<BalancedHost> getBalancedHosts() {
        return balancedHosts;
    }

    public void setBalancedHosts(List<BalancedHost> balancedHosts) {
        this.balancedHosts = balancedHosts;
    }

    public String getRequestTransformer() {
        return requestTransformer;
    }

    public void setRequestTransformer(String requestTransformer) {
        this.requestTransformer = requestTransformer;
    }

    public String getResponseTransformer() {
        return responseTransformer;
    }

    public void setResponseTransformer(String responseTransformer) {
        this.responseTransformer = responseTransformer;
    }

    public String getCors() {
        return cors;
    }

    public void setCors(String cors) {
        this.cors = cors;
    }

    public String getBody_schema() { return body_schema;}

    public void setBody_schema(String validation_schema) {this.body_schema = validation_schema;}

    public Boolean getVerbose_response() { return verbose_response;}

    public void setVerbose_response(Boolean verboseResponse) {this.verbose_response = verboseResponse;}

    public String getParameter_schema() {return parameter_schema;}

    public void setParameter_schema(String parameter_schema) {this.parameter_schema = parameter_schema;}

    public String getAllow_content_type() {return allow_content_type;}

    public void setAllow_content_type(String allow_content_type) {this.allow_content_type = allow_content_type;}

    public Boolean getEnable_validation_schema () {return enable_validation_schema;}
    public void setEnable_validation_schema (Boolean enable_validation_schema) {this.enable_validation_schema = enable_validation_schema;}

}