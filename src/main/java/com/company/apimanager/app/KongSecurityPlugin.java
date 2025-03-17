package com.company.apimanager.app;

import com.company.apimanager.app.kong_object.IpRestrictionSecurityPlugin;
import com.company.apimanager.app.kong_object.JwtAuthenticationPlugin;
import com.company.apimanager.app.kong_object.JwtAuthenticationPluginConfig;
import com.company.apimanager.app.kong_object.ResponseTransformerTransformationsPlugin;
import com.company.apimanager.screen.publishedproduct.HandledError;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Component
public class KongSecurityPlugin {
    private String kongManagementUrl;

    public KongSecurityPlugin() {
    }

    public KongSecurityPlugin(String kongManagementUrl) {
        this.kongManagementUrl = kongManagementUrl;
    }

    public String getKongManagementUrl() {
        return kongManagementUrl;
    }

    public void setKongManagementUrl(String kongManagementUrl) {
        this.kongManagementUrl = kongManagementUrl;
    }

    public int createIpRestrictionPlugin(String serviceName, StringBuilder pluginId, IpRestrictionSecurityPlugin plugin,
                                               HandledError handledError) {
        int retInt = 201;
        if (!StringUtils.hasText(serviceName) || plugin == null) {
            handledError.setErrorMsg("Parameter is Null or Empty when setting Ip Restriction");
            return -1;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String postStr = objectMapper.writeValueAsString(plugin);

            HttpEntity<String> request =
                    new HttpEntity<String>(postStr, headers);
            String kongServiceUrl = kongManagementUrl + "/services/" + serviceName + "/plugins";

            String resultAsJsonStr =
                    restTemplate.postForObject(kongServiceUrl, request, String.class);
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            String idString = convertedObject.get("id").getAsString();
            pluginId.setLength(0);
            pluginId.append(idString);
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -1;
        }
        return retInt;
    }

    public int updateIpRestrictionPlugin(String serviceName, IpRestrictionSecurityPlugin plugin, String pluginId, HandledError handledError) {
        if ( !StringUtils.hasText(serviceName) || plugin ==  null || !StringUtils.hasText(pluginId)) {
            handledError.setErrorMsg("Parameter is Null or Empty when updating Ip Restriction");
            return -1;
        }
        int retInt = 200;


        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);


        RestTemplate restTemplate = new RestTemplate();
        try {
            String putStr = objectMapper.writeValueAsString(plugin);

            String kongServiceUrl = kongManagementUrl + "/services/" + serviceName
                    + "/plugins/" + pluginId;
            HttpEntity<String> entity = new HttpEntity<String>(putStr, headers);
            String resultAsJsonStr = restTemplate.exchange(kongServiceUrl, HttpMethod.PUT, entity,
                    String.class).getBody();
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        } catch (Exception e) {
            retInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return retInt;
    }

}
