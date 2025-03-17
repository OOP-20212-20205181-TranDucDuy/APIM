package com.company.apimanager.app;

import com.company.apimanager.app.kong_object.ConsumerJwtCredential;
import com.company.apimanager.screen.publishedproduct.HandledError;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Component
public class KongConsumerCredential {
    private String kongManagementUrl;


    public KongConsumerCredential() {

    }

    public KongConsumerCredential(String kongManagementUrl) {
        this.kongManagementUrl = kongManagementUrl;
    }

    public String getKongManagementUrl() {
        return kongManagementUrl;
    }

    public void setKongManagementUrl(String kongManagementUrl) {
        this.kongManagementUrl = kongManagementUrl;
    }

    public int createOrUpdateJwtForConsumer(String consumerName, ConsumerJwtCredential jwt, StringBuilder keyId,
                                            HandledError handledError) {
        int createRet = this.createJwtForConsumer(consumerName, jwt, keyId, handledError);
        if (createRet == 201) { //Successful
            return 0;
        }
        if (createRet != 409) {
            handledError.setErrorMsg("Could not create jwt for consumer of " + consumerName
                    + ": " + handledError.getErrorMsg());
            return -1;
        }
        if (this.getJwtForConsumer(consumerName, jwt, keyId, handledError) != 200) {
            handledError.setErrorMsg("Could not get jwt for consumer of " + consumerName
                    + ": " + handledError.getErrorMsg());
            return -1;
        }
        if (this.updateJwtForConsumer(consumerName, jwt, keyId.toString(),
                handledError) != 200) {
            handledError.setErrorMsg("Could not update jwt for consumer: " + consumerName
                    + ": " + handledError.getErrorMsg());
            return -1;
        }
        return 0;
    }

    public int createJwtForConsumer(String consumerName, ConsumerJwtCredential jwt, StringBuilder keyId,
                                    HandledError handledError) {
        int retInt = 201;
        if ((consumerName.isEmpty()) || (consumerName == null) || (jwt == null)) {
            handledError.setErrorMsg("Null parameters");
            return -1;
        }

        String resultAsJsonStr = "";
        try {
            String kongServiceUrl = kongManagementUrl + "/consumers/" + consumerName + "/jwt";
            RestClient restTemplate = new RestClient();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String body = objectMapper.writeValueAsString(jwt);

            resultAsJsonStr = restTemplate.post(kongServiceUrl, body);

            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            keyId.setLength(0);
            keyId.append(convertedObject.get("key").getAsString());
        } catch (HttpClientErrorException httpClientErrorException) {
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -2;
        }
        return retInt;
    }

    public int getJwtForConsumer(String consumerName, ConsumerJwtCredential jwt, StringBuilder keyId,
                                 HandledError handledError) {
        int retInt = -1;
        if ((consumerName == null) || (jwt == null)) {
            handledError.setErrorMsg("Null parameters");
            return retInt;
        }
        if (consumerName.isEmpty()) {
            handledError.setErrorMsg("Null parameters");
            return retInt;
        }
        String resultAsJsonStr = "";
        try {
            String kongServiceUrl = kongManagementUrl + "/consumers/" + consumerName + "/jwt";

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response =
                    restTemplate.getForEntity(kongServiceUrl, String.class);
            JsonObject convertedObject = new Gson().fromJson(response.getBody(), JsonObject.class);
            JsonArray jsonArray = convertedObject.get("data").getAsJsonArray();

            if (jsonArray.size() == 0) {
                return 404;
            }
            boolean foundAuth = false;
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject authObject = jsonArray.get(i).getAsJsonObject();
                if (authObject.get("key").getAsString().equals(jwt.getKey())) {
                    foundAuth = true;
                    keyId.setLength(0);
                    keyId.append(authObject.get("id").getAsString());
                    break;
                }
            }
            if (foundAuth) {
                retInt = 200;
            } else {
                retInt = 404;
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -1;
        }
        return retInt;
    }

    public int updateJwtForConsumer(String consumerName, ConsumerJwtCredential jwt, String credentialId, HandledError handledError) {
        int retInt = 200;
        if ((consumerName == null) || (jwt == null)) {
            handledError.setErrorMsg("Null parameters");
            return retInt;
        }
        if (consumerName.isEmpty()) {
            handledError.setErrorMsg("Null parameters");
            return retInt;
        }

        try {
            String kongServiceUrl = kongManagementUrl + "/consumers/" + consumerName + "/jwt/" + credentialId;
            RestClient restTemplate = new RestClient();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String body = objectMapper.writeValueAsString(jwt);

            String resultAsJsonStr = restTemplate.put(kongServiceUrl, body);
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -2;
        }
        return retInt;
    }

}
