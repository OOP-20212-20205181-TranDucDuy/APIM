package com.company.apimanager.entity;

import com.company.apimanager.app.RestClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

import static com.company.apimanager.app.Utility.getString;
@JmixEntity
@Table(name = "PORTAL_SERVICE", indexes = {
        @Index(name = "IDX_PORTALSERVICE", columnList = "VIRTUAL_AREA_ID")
})
@Entity
public class PortalService {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME", nullable = false, unique = true, length = 50)
    @NotNull
    private String name;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "IS_DEFAULT")
    private Boolean is_default;

    @Column(name = "MANAGEMENT_ENDPOINT", nullable = false)
    @NotNull
    private String management_endpoint;

    @JoinColumn(name = "VIRTUAL_AREA_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private VirtualArea virtualArea;

    public VirtualArea getVirtualArea() {
        return virtualArea;
    }

    public void setVirtualArea(VirtualArea virtualArea) {
        this.virtualArea = virtualArea;
    }

    public String getManagement_endpoint() {
        return management_endpoint;
    }

    public void setManagement_endpoint(String management_endpoint) {
        this.management_endpoint = management_endpoint;
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

    public Boolean getIs_default() { return is_default; }

    public void setIs_default(Boolean is_default) { this.is_default = is_default; }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public static String createPortal(String baseUrl, String userId, String accessToken, String portalId) {
        RestClient restTemplate = new RestClient();

        String createPortalBody = "{\n" +
                "    \"_entityName\": \"PortalUsagePermission\",\n" +
                "    \"_instanceName\": \"Portal Default\",\n" +
                "    \"name\": \"Portal Default\",\n" +
                "    \"provider_portal_id\": \"" + userId + "_" + portalId + "\",\n" +
                "    \"api_provider\": {\n" +
                "        \"id\": \"" + userId + "\"\n" +
                "    },\n" +
                "    \"portal\": {\n" +
                "        \"id\": \"" + portalId + "\"\n" +
                "    }\n" +
                "}";

        String createPortalUrl = baseUrl + "/rest/entities/PortalUsagePermission";
        return restTemplate.postToken(createPortalUrl, createPortalBody, "Authorization", "Bearer " + accessToken);
    }

    public static String searchPortal(String baseUrl, String virtualAreaId, String accessToken) {
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

        String searchPortalUrl = baseUrl + "/rest/entities/PortalService/search";
        return getString(accessToken, restTemplate, searchBody, searchPortalUrl);
    }

    public static String searchPortalById(String baseUrl, String accessToken, String id){
        RestClient restTemplate = new RestClient();

        String searchBody = "{\n" +
                "    \"filter\": {\n" +
                "        \"conditions\": [\n" +
                "            {\n" +
                "                \"property\": \"id\",\n" +
                "                \"operator\": \"=\",\n" +
                "                \"value\": \"" + id + "\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        String searchPortalUrl = baseUrl + "/rest/entities/PortalService/search";
        String searchResult = restTemplate.postToken(searchPortalUrl, searchBody, "Authorization", "Bearer " + accessToken);
        JsonArray portal = new Gson().fromJson(searchResult, JsonArray.class);
        return portal.get(0).getAsJsonObject().get("management_endpoint").getAsString();
    }
}