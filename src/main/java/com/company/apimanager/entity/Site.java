package com.company.apimanager.entity;

import com.company.apimanager.app.RestClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "SITE", indexes = {
        @Index(name = "IDX_SITE_OWNER_ID", columnList = "OWNER_ID"),
        @Index(name = "IDX_SITE_VIRTUAL_AREA_ID", columnList = "VIRTUAL_AREA_ID"),
        @Index(name = "IDX_SITE_GATEWAY_ID", columnList = "GATEWAY_ID"),
        @Index(name = "IDX_SITE_PORTAL_ID", columnList = "PORTAL_ID")
})
@Entity
public class Site {
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

    @JoinColumn(name = "OWNER_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User owner;

    @Column(name = "PORTAL_ADMIN", nullable = false, length = 50)
    @NotNull
    private String portal_admin;

    @Column(name = "OAUTH_PROVIDER")
    private String oauth_provider;

    @Column(name = "LDAP_URL")
    private String ldap_url;

    @Column(name = "LDAP_PRINCIPAL")
    private String ldap_principal;

    @Column(name = "LDAP_CREDENTIAL")
    private String ldap_credential;

    @Column(name = "Site_Logo")
    private String site_logo;

    @Column(name = "PRODUCTION_MODE", nullable = false)
    @NotNull
    private Boolean production_mode;

    @JoinColumn(name = "GATEWAY_ID")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private GatewayService gateway;

    @JoinColumn(name = "PORTAL_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PortalService portal;

    @Column(name = "OWNER_SITENAME", nullable = false, unique = true)
    @NotNull
    private String owner_sitename;

    @JoinColumn(name = "VIRTUAL_AREA_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private VirtualArea virtual_area;

    @Column(name = "SELF_HOSTED_GATEWAY_USED", nullable = false)
    @NotNull
    private Boolean self_hosted_gateway_used;

    @JoinColumn(name = "SELF_HOSTED_GATEWAY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private SelfHostedGateway self_hosted_gateway;

    @Column(name = "DRUPAL_API")
    private String drupal_api;

    @Column(name = "PORTAL_ABOUT_PAGE_CONTENT")
    private String portal_about_page_content;

    @Column(name = "PORTAL_HOME_PAGE_PAGE_CONTENT")
    private String portal_home_page_content;

    @Column(name = "PORTAL_HOME_PAGE_PAGE_TITLE")
    private String portal_home_page_title;

    @Column(name = "PORTAL_ABOUT_PAGE_IMAGE_ID")
    private String portal_about_page_image_id;

    @Column(name = "PORTAL_ABOUT_PAGE_IMAGE_URL")
    private String portal_about_page_image_url;

    public static String ENDPOINT_CREATE_PORTAL = "http://flask-service:5000";
    public static String ABOUT_PAGE_ID = "3";
    public static String HOME_PAGE_ID = "4";
    public static String PRODUCT_PAGE_ID = "5";
    public static String DRUPAL_ADMIN_USER = "ductoan1";
    public static String DRUPAL_ADMIN_PASSWORD = "ductoan1";

    //

    public static final Logger logger = LoggerFactory.getLogger(Site.class);

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PortalService getPortal() {
        return portal;
    }

    public void setPortal(PortalService portal) {
        this.portal = portal;
    }

    public GatewayService getGateway() {
        return gateway;
    }

    public void setGateway(GatewayService gateway) {
        this.gateway = gateway;
    }

    public String getOauth_provider() {
        return oauth_provider;
    }

    public void setOauth_provider(String oauth_provider) {
        this.oauth_provider = oauth_provider;
    }

    public String getPortal_admin() {
        return portal_admin;
    }

    public void setPortal_admin(String portal_admin) {
        this.portal_admin = portal_admin;
    }

    public VirtualArea getVirtual_area() {
        return virtual_area;
    }

    public void setVirtual_area(VirtualArea virtual_area) {
        this.virtual_area = virtual_area;
    }

    public String getOwner_sitename() {
        return owner_sitename;
    }

    public void setOwner_sitename(String owner_sitename) {
        this.owner_sitename = owner_sitename;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
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

    public Boolean getProduction_mode() {
            return production_mode;
    }

    public void setLdap_url(String ldap_url) {
        this.ldap_url = ldap_url;
    }

    public String getLdap_url() {
        return ldap_url;
    }

    public String getLdap_credential() {
        return ldap_credential;
    }

    public String getLdap_principal() {
        return ldap_principal;
    }

    public void setLdap_credential(String ldap_credential) {
        this.ldap_credential = ldap_credential;
    }

    public void setLdap_principal(String ldap_principal) {
        this.ldap_principal = ldap_principal;
    }

    public String getSite_logo() {
        return site_logo;
    }

    public void setSite_logo(String site_logo) {
        this.site_logo = site_logo;
    }

    public void setProduction_mode(Boolean production_mode) {
        this.production_mode = production_mode;
    }

    public void setSelf_hosted_gateway_used(Boolean self_hosted_gateway_used) {
        this.self_hosted_gateway_used = self_hosted_gateway_used;
    }

    public void setSelf_hosted_gateway(SelfHostedGateway self_hosted_gateway) {
        this.self_hosted_gateway = self_hosted_gateway;
    }

    public SelfHostedGateway getSelf_hosted_gateway() {
        return self_hosted_gateway;
    }

    public void setDrupal_api(String api) {
        this.drupal_api = api;
    }

    public String getDrupal_api() {
        return this.drupal_api;
    }


    public static void setHomePageId(String homePageId) {
        HOME_PAGE_ID = homePageId;
    }

    public static void setProductPageId(String productPageId) {
        PRODUCT_PAGE_ID = productPageId;
    }

    public static void setDrupalAdminUser(String drupalAdminUser) {
        DRUPAL_ADMIN_USER = drupalAdminUser;
    }

    public static String getDrupalAdminPassword() {
        return DRUPAL_ADMIN_PASSWORD;
    }
    
    public static String getAboutPageId() {
        return ABOUT_PAGE_ID;
    }

    public static String getEndpointCreatePortal() {
        return ENDPOINT_CREATE_PORTAL;
    }

    public String getPortal_about_page_content() {
        return portal_about_page_content;
    }

    public String getPortal_home_page_content() {
        return portal_home_page_content;
    }

    public void setPortal_home_page_content(String portal_home_page_content) {
        this.portal_home_page_content = portal_home_page_content;
    }

    public void setPortal_about_page_content(String portal_about_page_content) {
        this.portal_about_page_content = portal_about_page_content;
    }

    public String getPortal_home_page_title() {
        return portal_home_page_title;
    }

    public void setPortal_home_page_title(String portal_home_page_title) {
        this.portal_home_page_title = portal_home_page_title;
    }

    public String getPortal_about_page_image_id() {
        return portal_about_page_image_id;
    }

    public String getPortal_about_page_image_url() {
        return portal_about_page_image_url;
    }

    public void setPortal_about_page_image_id(String portal_about_page_image_id) {
        this.portal_about_page_image_id = portal_about_page_image_id;
    }

    public void setPortal_about_page_image_url(String portal_about_page_image_url) {
        this.portal_about_page_image_url = portal_about_page_image_url;
    }

    public Boolean getSelf_hosted_gateway_used() {
        return self_hosted_gateway_used;
    }

    public String getDefaultManagementEndpoint() {
        String gatewayManagementEndpoint = null;
        if (self_hosted_gateway_used) {
            if (self_hosted_gateway == null) {

            }
            else {
                gatewayManagementEndpoint = self_hosted_gateway.getManagement_endpoint();
            }
        }
        else {
            if (gateway == null) {

            }
            else {
                gatewayManagementEndpoint = gateway.getManagement_endpoint();
            }
        }
        return gatewayManagementEndpoint;
    }

    public String getDefaultInvocationEndpoint() {
        String gatewayInvocationEndpoint = null;
        if (self_hosted_gateway_used) {
            if (self_hosted_gateway == null) {

            }
            else {
                gatewayInvocationEndpoint = self_hosted_gateway.getInvocation_endpoint();
            }
        }
        else {
            if (gateway == null) {

            }
            else {
                gatewayInvocationEndpoint = gateway.getInvocation_endpoint();
            }
        }
        return gatewayInvocationEndpoint;
    }

    public static String createSite(String baseUrl, String userId, String accessToken, String virtualAreaId, String username) {
        RestClient restTemplate = new RestClient();
        String portalId = PortalService.searchPortal(baseUrl, virtualAreaId, accessToken);
        String gatewayId = GatewayService.searchGateway(baseUrl, virtualAreaId, accessToken);
        String portalEndpoint = PortalService.searchPortalById(baseUrl, accessToken, portalId);
        String url = Site.createPortal(username, portalEndpoint);

        String createSiteBody = " {\n" +
                "    \"_entityName\": \"Site\",\n" +
                "    \"_instanceName\": \"Public\",\n" +
                "    \"self_hosted_gateway_used\": false,\n" +
                "    \"title\": \"Public site\",\n" +
                "    \"portal_admin\": \"admin\",\n" +
                "    \"drupal_api\": \"https://" + url + "\",\n" +
                "    \"name\": \"Public\",\n" +
                "    \"oauth_provider\": \"\",\n" +
                "    \"owner_sitename\": \"" + username + "_Public\",\n" +
                "    \"production_mode\": false,\n" +
                "\t \"owner\": {\n" +
                "        \"id\": \"" + userId + "\"\n" +
                "    },\n" +
                "    \"gateway\": {\n" +
                "        \"id\": \"" + gatewayId + "\"\n" +
                "    },\n" +
                "    \"portal\": {\n" +
                "        \"id\": \"" + portalId + "\"\n" +
                "    },\n" +
                "    \"virtual_area\": {\n" +
                "        \"id\": \"" + virtualAreaId + "\"\n" +
                "    }\n" +
                "}";

        String createSiteUrl = baseUrl + "/rest/entities/Site";
        return restTemplate.postToken(createSiteUrl, createSiteBody, "Authorization", "Bearer " + accessToken);
    }

    public static String createPortal(String name, String portalEndpoint){
        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            String postStr = "{\n" +
                    "    \"name\": \"" + name + "\"\n" +
                    "}";
            logger.info("Body create portal: " + postStr);
            HttpEntity<String> request = new HttpEntity<String>(postStr, headers);

            String createPortalUrl = portalEndpoint + "/createportal";
            String resultAsJsonStr = restTemplate.postForObject(createPortalUrl, request, String.class);
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            logger.info("Response from create portal api: " + resultAsJsonStr);
            if (convertedObject.get("success").getAsBoolean()) {
                return convertedObject.get("url").getAsString();
            }
        } catch (Exception e) {
            logger.error("Error when create portal. Reason: " + e.getMessage());
        }

        return "";
    }
}