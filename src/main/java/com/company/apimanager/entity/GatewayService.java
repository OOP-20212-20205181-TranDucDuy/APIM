package com.company.apimanager.entity;

import com.company.apimanager.app.RestClient;
import com.company.apimanager.screen.publishedproduct.HandledError;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.UUID;

import static com.company.apimanager.app.Utility.getString;

@JmixEntity
@Table(name = "GATEWAY_SERVICE", indexes = {
        @Index(name = "IDX_GATEWAYSERVICE", columnList = "VIRTUAL_AREA_ID"),
        @Index(name = "IDX_GATEWAYSERVICE", columnList = "GATEWAY_TYPE_ID"),
        @Index(name = "IDX_GATEWAYSERVICE", columnList = "ANALYTICS_ASSOCIATED_ID")
})
@Entity
public class GatewayService {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME", nullable = false, unique = true, length = 50)
    @NotNull
    private String name;

    @Column(name = "MANAGEMENT_ENDPOINT", nullable = false)
    @NotNull
    private String management_endpoint;

    @Column(name = "INVOCATION_ENDPOINT")
    private String invocation_endpoint;

    @JoinColumn(name = "GATEWAY_TYPE_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private GatewayType gateway_type;

    @Column(name = "TITLE", length = 100)
    private String title;

    @JoinColumn(name = "ANALYTICS_ASSOCIATED_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private AnalyticsService analyticsAssociated;

    @JoinColumn(name = "VIRTUAL_AREA_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private VirtualArea virtualArea;

    @Column(name = "IP_ADDRESS", length = 100)
    private String ip_address;

    public AnalyticsService getAnalyticsAssociated() {
        return analyticsAssociated;
    }

    public void setAnalyticsAssociated(AnalyticsService analyticsAssociated) {
        this.analyticsAssociated = analyticsAssociated;
    }

    public GatewayType getGateway_type() {
        return gateway_type;
    }

    public void setGateway_type(GatewayType gateway_type) {
        this.gateway_type = gateway_type;
    }

    public VirtualArea getVirtualArea() {
        return virtualArea;
    }

    public void setVirtualArea(VirtualArea virtualArea) {
        this.virtualArea = virtualArea;
    }

    public String getInvocation_endpoint() {
        return invocation_endpoint;
    }

    public void setInvocation_endpoint(String invocation_endpoint) {
        this.invocation_endpoint = invocation_endpoint;
    }

    public String getManagement_endpoint() {
        return management_endpoint;
    }

    public void setManagement_endpoint(String management_endpoint) {
        this.management_endpoint = management_endpoint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp_address() { return ip_address; }

    public void setIp_address(String ip_address) { this.ip_address = ip_address; }

    @Column(name = "IS_DEFAULT")
    private Boolean is_default;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Boolean getIs_default() { return is_default; }

    public void setIs_default(Boolean is_default) { this.is_default = is_default; }

    public static String createGw(String baseUrl, String userId, String accessToken, String gatewayId) {
        RestClient restTemplate = new RestClient();

        String createGwBody = "{\n" +
                "\n" +
                "        \"_entityName\": \"GatewayUsagePermission\",\n" +
                "        \"_instanceName\": \"Gateway Default\",\n" +
                "        \"name\": \"Gateway Default\",\n" +
                "        \"provider_gateway_id\": \"" + userId + "_5efc53fc-71a1-7779-ce9f-021a22f7e679\",\n" +
                "\t\"api_provider\": {\n" +
                "\t\t\"id\": \"" + userId + "\"\n" +
                "\t},\n" +
                "\t\"gateway\":  {\n" +
                "\t\t\"id\": \"" + gatewayId + "\"\n" +
                "\t}\n" +
                "    }";

        String createGwUrl = baseUrl + "/rest/entities/GatewayUsagePermission";
        return restTemplate.postToken(createGwUrl, createGwBody, "Authorization", "Bearer " + accessToken);
    }

    public static String searchGateway(String baseUrl, String virtualAreaId, String accessToken) {
        RestClient restTemplate = new RestClient();

        String searchBody = "{\n" +
                "    \"filter\": {\n" +
                "        \"conditions\": [\n" +
                "            {\n" +
                "                \"property\": \"virtualArea\",\n" +
                "                \"operator\": \"=\",\n" +
                "                \"value\": \"" + virtualAreaId + "\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"property\": \"is_default\",\n" +
                "                \"operator\": \"=\",\n" +
                "                \"value\": true\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        String searchGatewayUrl = baseUrl + "/rest/entities/GatewayService/search";
        return getString(accessToken, restTemplate, searchBody, searchGatewayUrl);
    }

    public static void createGateway(String username, String siteName, String ip, String gwEndpoint, String createGWEndpoint, HandledError handledError){
        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            String postStr = """
                    {
                        "username": "%s",
                        "site_name": "%s",
                        "ip": "%s",
                        "gw_endpoint": "%s"
                    }""".formatted(username, siteName, ip, gwEndpoint);

            HttpEntity<String> request = new HttpEntity<String>(postStr, headers);

            String resultAsJsonStr = restTemplate.postForObject(createGWEndpoint, request, String.class);
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);

            if (convertedObject.get("success").getAsBoolean()) {
                convertedObject.get("url").getAsString();
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
        }
    }
}