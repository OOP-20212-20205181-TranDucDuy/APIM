package com.company.apimanager.entity;

import com.company.apimanager.app.KongOAuthClient;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@JmixEntity
@Table(name = "CONSUMER", indexes = {
        @Index(name = "IDX_CONSUMER_PROVIDER_ID", columnList = "PROVIDER_ID"),
        @Index(name = "IDX_CONSUMER_SITE_ID", columnList = "SITE_ID")
})
@Entity
public class Consumer {
    @JmixGeneratedValue

    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME", nullable = false, length = 255)
    @NotNull
    private String name;

    @JoinColumn(name = "SITE_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Site site;

    @Column(name = "API_KEY")
    private String api_key;

    @Column(name = "TITLE", nullable = false)
    @NotNull
    private String title;

    @JoinColumn(name = "PROVIDER_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User provider;

    @Column(name = "PASSWORD", length = 255)
    private String password;

    @Column(name = "PROVIDER_SITE_CONSUMER_NAME", nullable = false)
    @NotNull
    private String provider_site_consumer_name;

    @Column(name = "KONG_CONSUMER_ID", nullable = false)
    @NotNull
    private String kong_consumer_id;

    @Column(name = "MAX_PER_SECOND")
    private Long max_per_second;

    @Column(name = "MAX_PER_MINUTE")
    private Long max_per_minute;

    @Column(name = "MAX_PER_HOUR")
    private Long max_per_hour;

    @Column(name = "KONG_RATE_LIMIT_ID")
    private String kong_rate_limit_id;

    @Column(name = "BASE_CREDENTIAL_ID")
    private String base_credential_id;

    @Column(name = "KEY_CREDENTIAL_ID")
    private String key_credential_id;

    @Column(name = "KONG_OAUTH_ID")
    private String kong_oauth_id;

    @Column(name = "OAUTH_CLIENT_ID")
    private String oauth_client_id;

    @Column(name = "OAUTH_CLIENT_SECRET")
    private String oauth_client_secret;

    @Column(name = "OAUTH_CLIENT_NAME")
    private String oauth_client_name;

    @Column(name = "OAUTH_REDIRECT_URI")
    private String oauth_redirect_uri;

    @Column(name = "JWT_SECRET", length = 1000)
    private String jwtSecret;

    @Column(name = "JWT_ALGORITHM")
    private String jwtAlgorithm;

    @Column(name = "JWT_RSA_PUBLIC_KEY", length = 1000)
    private String jwtRsaPublicKey;

    @Column(name = "JWT_CLIENT_ID")
    private String jwtClientId;

    public String getJwtAlgorithm() {
        return jwtAlgorithm;
    }

    public void setJwtAlgorithm(String jwtAlgorithm) {
        this.jwtAlgorithm = jwtAlgorithm;
    }

    public String getJwtClientId() {
        return jwtClientId;
    }

    public void setJwtClientId(String jwtClientId) {
        this.jwtClientId = jwtClientId;
    }

    public String getJwtRsaPublicKey() {
        return jwtRsaPublicKey;
    }

    public void setJwtRsaPublicKey(String jwtRsaPublicKey) {
        this.jwtRsaPublicKey = jwtRsaPublicKey;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }



    public void setMax_per_minute(Long max_per_minute) {
        this.max_per_minute = max_per_minute;
    }

    public Long getMax_per_minute() {
        return max_per_minute;
    }

    public String getKong_rate_limit_id() {
        return kong_rate_limit_id;
    }

    public void setKong_rate_limit_id(String kong_rate_limit_id) {
        this.kong_rate_limit_id = kong_rate_limit_id;
    }

    public Long getMax_per_hour() {
        return max_per_hour;
    }

    public void setMax_per_hour(Long max_per_hour) {
        this.max_per_hour = max_per_hour;
    }

    public Long getMax_per_second() {
        return max_per_second;
    }

    public void setMax_per_second(Long max_per_second) {
        this.max_per_second = max_per_second;
    }

    public String getKong_consumer_id() {
        return kong_consumer_id;
    }

    public void setKong_consumer_id(String kong_consumer_id) {
        this.kong_consumer_id = kong_consumer_id;
    }

    public String getProvider_site_consumer_name() {
        return provider_site_consumer_name;
    }

    public void setProvider_site_consumer_name(String provider_site_consumer_name) {
        this.provider_site_consumer_name = provider_site_consumer_name;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProvider_consumer_name() {
        return provider_site_consumer_name;
    }

    public void setProvider_consumer_name(String provider_consumer_name) {
        this.provider_site_consumer_name = provider_consumer_name;
    }

    public User getProvider() {
        return provider;
    }

    public void setProvider(User provider) {
        this.provider = provider;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBase_credential_id() {
        return base_credential_id;
    }

    public void setBaseCredentialId(String base_credential_id) {
        this.base_credential_id = base_credential_id;
    }

    public String getKey_credential_id() {
        return key_credential_id;
    }

    public void setKeyCredentialId(String key_credential_id) {
        this.key_credential_id = key_credential_id;
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

    public void setKey_credential_id(String key_credential_id) {
        this.key_credential_id = key_credential_id;
    }

    public void setOauth_redirect_uri(String oauth_redirect_uri) {
        this.oauth_redirect_uri = oauth_redirect_uri;
    }

    public void setBase_credential_id(String base_credential_id) {
        this.base_credential_id = base_credential_id;
    }

    public String getOauth_redirect_uri() {
        return oauth_redirect_uri;
    }

    public String getKong_oauth_id() {
        return kong_oauth_id;
    }

    public String getOauth_client_id() {
        return oauth_client_id;
    }

    public String getOauth_client_name() {
        return oauth_client_name;
    }

    public String getOauth_client_secret() {
        return oauth_client_secret;
    }

    public void setKong_oauth_id(String kong_oauth_id) {
        this.kong_oauth_id = kong_oauth_id;
    }

    public void setOauth_client_id(String oauth_client_id) {
        this.oauth_client_id = oauth_client_id;
    }

    public void setOauth_client_name(String oauth_client_name) {
        this.oauth_client_name = oauth_client_name;
    }

    public void setOauth_client_secret(String oauth_client_secret) {
        this.oauth_client_secret = oauth_client_secret;
    }

    public void setOAuthInfo(KongOAuthClient kongOAuthClient) {
        if (kongOAuthClient == null) {
            return;
        }
        this.oauth_redirect_uri = kongOAuthClient.getClientRedirectUri();
        this.oauth_client_id = kongOAuthClient.getClientId();
        this.oauth_client_name = kongOAuthClient.getClientName();
        this.oauth_client_secret = kongOAuthClient.getClientSecret();
        this.kong_oauth_id = kongOAuthClient.getKongId();
    }

    public void setJwtInfo(JwtClient jwt) {
        if (jwt == null) {
            return;
        }
        this.jwtSecret = jwt.getSecret();
        this.jwtRsaPublicKey = jwt.getRsa_public_key();
        this.jwtAlgorithm = jwt.getAlgorithm();
        this.jwtClientId = jwt.getId();
    }
}