package com.company.apimanager.app.kong_action;

import com.company.apimanager.app.KongServiceDetails;
import com.company.apimanager.app.KongServicePlugin;
import com.company.apimanager.screen.publishedproduct.HandledError;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

public class Helper {
    private static final String OAUTH_PLUGIN_NAME = "oauth2";
    private String kongManagementUrl;
    private String pluginType;

    public KongServicePlugin getServicePluginByType(String serviceName, String pluginType,
                                                    HandledError handledError) {
        KongServicePlugin[] kongServicePlugins = this.getServicePlugins(serviceName, handledError);
        if (kongServicePlugins == null) {
            return null;
        }
        boolean foundPlugin = false;
        int indInt = -1;
        for (int i = 0; i < kongServicePlugins.length; i++) {
            if (kongServicePlugins[i].getPluginName().equals(pluginType)) {
                foundPlugin = true;
                indInt = i;
                break;
            }
        }
        if (!foundPlugin) {
            return null;
        }
        return kongServicePlugins[indInt];
    }

    public KongServicePlugin[] getServicePlugins(String serviceName, HandledError handledError) {
        if (serviceName == null) {
            handledError.setErrorMsg("Service name is Null");
            return null;
        }
        if (serviceName.isEmpty()) {
            handledError.setErrorMsg("Service name is Empty");
            return null;
        }
        KongServicePlugin[] kongServicePlugins = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response =
                    restTemplate.getForEntity(
                            kongManagementUrl + "/services/" + serviceName + "/plugins",
                            String.class);
            JsonObject convertedObject = new Gson().fromJson(response.getBody(), JsonObject.class);
            JsonArray jsonArray = convertedObject.get("data").getAsJsonArray();
            if (jsonArray.size() > 0) {
                kongServicePlugins = new KongServicePlugin[jsonArray.size()];
                for (int i = 0; i < jsonArray.size(); i++) {
                    String idStr = jsonArray.get(i).getAsJsonObject().get("id").getAsString();
                    String nameStr = jsonArray.get(i).getAsJsonObject().get("name").getAsString();
                    kongServicePlugins[i] = new KongServicePlugin();
                    kongServicePlugins[i].setKongId(idStr);
                    kongServicePlugins[i].setPluginName(nameStr);
                    this.getServicePluginInfo(jsonArray.get(i).getAsJsonObject(), kongServicePlugins[i]);
                }
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            kongServicePlugins = null;
        } catch (Exception e) {
            kongServicePlugins = null;
        }
        return kongServicePlugins;
    }

    private void getServicePluginInfo(JsonObject jsonObject, KongServicePlugin kongServicePlugin) {
        try {
            String pluginName = jsonObject.get("name").getAsString();
            if (pluginName.equals(OAUTH_PLUGIN_NAME)) {
                String provisionKey = jsonObject.get("config").getAsJsonObject().get("provision_key").getAsString();
                kongServicePlugin.setOauthProvisionKey(provisionKey);
            }
        } catch (Exception e) {

        }
    }

    public int deletePluginWithType(String serviceName, String pluginType, HandledError handledError) {
        int retInt = -1;
        if (!StringUtils.hasText(serviceName)) {
            handledError.setErrorMsg("Service Name id is Null or Empty");
            return retInt;
        }
        try {
            KongServicePlugin kongServicePlugin = this.getServicePluginByType(serviceName, pluginType, handledError);
            if (kongServicePlugin != null) {
                retInt = deletePlugin(kongServicePlugin.getKongId(), handledError);
            } else {
                return 204;
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            retInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return retInt;
    }

    public int deletePlugin(String pluginId, HandledError handledError) {
        int retInt = -1;
        if (!StringUtils.hasText(pluginId)) {
            handledError.setErrorMsg("Plugin id is Null or Empty");
            return -1;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String deleteUrl = kongManagementUrl + "/plugins/" + pluginId;

            HttpHeaders headers = new HttpHeaders();

            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<KongServiceDetails> entity = new HttpEntity<KongServiceDetails>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);
            retInt = response.getStatusCodeValue(); //204 OK
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            handledError.setErrorMsg(httpClientErrorException.getMessage());
        } catch (Exception e) {
            retInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return retInt;
    }

    public int createPlugin(String serviceName, StringBuilder pluginId,
                            HandledError handledError, String value) {
        int retInt = 201;
        if (!StringUtils.hasText(serviceName)) {
            handledError.setErrorMsg("Service Name is Null or Empty when setting" + this.pluginType);
            return -1;
        }
        if (!StringUtils.hasText(value)) {
            handledError.setErrorMsg("Value is Null or Empty when setting" + this.pluginType);
            return -1;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            String postStr = value;


            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

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

    public int updatePlugin(String serviceName, String pluginId, HandledError handledError, String putStr) {
        if (serviceName == null || putStr == null || pluginId == null) {
            handledError.setErrorMsg("Parameter is Null when updating " + this.pluginType);
            return -1;
        }
        if (serviceName.isEmpty() || putStr.isEmpty() || putStr.isBlank() || pluginId.isEmpty() || serviceName.isBlank()) {
            handledError.setErrorMsg("Parameter is Empty or Blank when updating " + this.pluginType);
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


    public Helper(String kongManagementUrl, String pluginType) {
        this.kongManagementUrl = kongManagementUrl;
        this.pluginType = pluginType;
    }

    public String getKongManagementUrl() {
        return kongManagementUrl;
    }

    public void setKongManagementUrl(String kongManagementUrl) {
        this.kongManagementUrl = kongManagementUrl;
    }
}
