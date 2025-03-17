package com.company.apimanager.app;

import com.company.apimanager.app.kong_object.KongConsumerGroup;
import com.company.apimanager.app.kong_object.KongConsumerGroupRatelimit;
import com.company.apimanager.screen.publishedproduct.HandledError;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.http.HttpHeaders;

import org.springframework.http.*;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

public class KongConsumerGroupController {
    private String kongManagementUrl;

    public String getKongManagementUrl() {
        return kongManagementUrl;
    }

    public void setKongManagementUrl(String kongManagementUrl) {
        this.kongManagementUrl = kongManagementUrl;
    }

    public KongConsumerGroupController(String kongManagementUrl){
        this.kongManagementUrl = kongManagementUrl;
    }
    public boolean validString(String s) {
        return (s != null && !s.equals(""));
    }
    public String getGroupPath(KongConsumerGroup group){
        String id = group.getId(), name = group.getName();
        if (validString(id)){
            return id;
        }
        if (validString(name)){
            return name;
        }
        return null;
    }

    public String getConsumerPath(KongConsumerDetails consumer){
        String id = consumer.getId();
        String userName = consumer.getUserName();
        if (validString(id)){
            return id;
        }
        if (validString(userName)){
            return userName;
        }
        return null;
    }

    public KongConsumerGroup createConsumerGroup(String groupName, HandledError handledError){
        if (!validString(groupName)) {
            handledError.setErrorMsg("New consumer group required field Name");
            return null;
        }
        try{
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            MultiValueMap<String, String> data;
            data = new LinkedMultiValueMap<String, String>();
            data.add("name", groupName);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(data, headers);
            String kongConsumerGroupUrl = kongManagementUrl + "/consumer_groups";
            ResponseEntity<KongConsumerGroup> response = restTemplate.postForEntity(kongConsumerGroupUrl, request, KongConsumerGroup.class);
            KongConsumerGroup group = new KongConsumerGroup();
            group.setId(response.getBody().getId());
            group.setName(response.getBody().getName());
            group.setCreated_at(response.getBody().getCreated_at());
            return group;
        }
        catch (HttpClientErrorException httpClientErrorException){
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
            return null;
        }
        catch (Exception e){
            handledError.setErrorMsg(e.getMessage());
            return null;
        }
    }
    public KongConsumerGroup getConsumerGroupByName(String groupName, HandledError handledError){
        KongConsumerGroup group;
        if (!validString(groupName)){
            handledError.setErrorMsg("Consumer group require a Name.");
            handledError.setHttpResponseCode(-1);
            return null;
        }
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<KongConsumerGroup> response =
                restTemplate.getForEntity(
                        kongManagementUrl + "/consumer_groups/" + groupName,
                        KongConsumerGroup.class);
            group = response.getBody();
            handledError.setHttpResponseCode(response.getStatusCodeValue());
        } catch (HttpClientErrorException httpClientErrorException) {
            group = null;
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        } catch (Exception e) {
            group = null;
            handledError.setErrorMsg(e.getMessage());
            handledError.setHttpResponseCode(-1);
        }
        return group;
    }
    public int deleteConsumerGroup(KongConsumerGroup group, HandledError handledError){
        int resInt = -1;
        String groupPath = getGroupPath(group);
        if (groupPath==null) {
            handledError.setErrorMsg("Consumer group required ID or Name.");
            return resInt;
        }
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.delete(kongManagementUrl+"/consumer_groups/"+groupPath);
            resInt = 200;
        } catch (HttpClientErrorException httpClientErrorException){
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
            resInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e){
            resInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return resInt;
    }

    public int addConsumerToGroup(KongConsumerGroup group, KongConsumerDetails consumer, HandledError handledError){
        String groupPath = getGroupPath(group), consumerPath = getConsumerPath(consumer);
        int resInt = -1;
        if (groupPath==null){
            handledError.setErrorMsg("Consumer Group requires ID or Name.");
            return resInt;
        }
        if (consumerPath == null){
            handledError.setErrorMsg("Consumer requires ID or Username.");
            return resInt;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            RestTemplate restTemplate = new RestTemplate();
            MultiValueMap<String, String> data;
            data = new LinkedMultiValueMap<String, String>();
            data.add("consumer.id", consumer.getId());
            data.add("consumer.username", consumer.getUserName());
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(data, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(kongManagementUrl+"/consumer_groups/"+groupPath+"/consumers", request, String.class);
            resInt = response.getStatusCodeValue();
        } catch (HttpClientErrorException httpClientErrorException){
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
            resInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e){
            resInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return resInt;
    }

    public int deleteConsumerFromGroup(KongConsumerGroup group, KongConsumerDetails consumer, HandledError handledError){
        int resInt = -1;
        String groupPath= getGroupPath(group), consumerPath = getConsumerPath(consumer);
        if (groupPath == null){
            handledError.setErrorMsg("Consumer group require Name or ID");
            return resInt;
        }
        if (consumerPath == null){
            handledError.setErrorMsg("Consumer require ID or Username");
            return resInt;
        }
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.delete(kongManagementUrl+"/consumer_groups/"+groupPath+"/consumers/"+consumerPath);
            resInt = 200;
        } catch (HttpClientErrorException httpClientErrorException){
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
            resInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e){
            resInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return resInt;
    }

    public int setRateLimitOnGroup(KongConsumerGroup group, KongConsumerGroupRatelimit rateLimit, HandledError handledError){
        int resInt = -1;
        String groupPath = getGroupPath(group);
        if (groupPath == null){
            handledError.setErrorMsg("Consumer group requires ID or Name.");
            return resInt;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            RestTemplate restTemplate = new RestTemplate();
            MultiValueMap<String, Integer> data;
            data = new LinkedMultiValueMap<String, Integer>();
            if (rateLimit.getSecond() > 0){
                data.add("second", rateLimit.getSecond());
            }
            if (rateLimit.getMinute()>rateLimit.getSecond()){
                data.add("minute", rateLimit.getMinute());
            }
            if (rateLimit.getHour()>rateLimit.getMinute()){
                data.add("hour", rateLimit.getHour());
            }
            if (rateLimit.getDay()>rateLimit.getHour()){
                data.add("day", rateLimit.getDay());
            }
            if (rateLimit.getMonth()>rateLimit.getDay()) {
                data.add("month", rateLimit.getMonth());
            }
            if (rateLimit.getYear()>rateLimit.getYear()) {
                data.add("year", rateLimit.getYear());
            }
            HttpEntity<MultiValueMap<String, Integer>> request = new HttpEntity<MultiValueMap<String, Integer>>(data, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(kongManagementUrl+"/consumer_groups/"+groupPath+"/ratelimiting", request, String.class);
            resInt = response.getStatusCodeValue();
        } catch (HttpClientErrorException httpClientErrorException){
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
            resInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e){
            resInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return resInt;
    }

    public KongConsumerGroupRatelimit getRateLimitOnGroup(KongConsumerGroup group, HandledError handledError){
        KongConsumerGroupRatelimit rateLimit = new KongConsumerGroupRatelimit();
        String groupPath = getGroupPath(group);
        if (groupPath == null){
            handledError.setErrorMsg("Consumer Group require ID or Name.");
            return null;
        }
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(kongManagementUrl+"/consumer_groups/"+groupPath+"/ratelimiting", String.class);
            JsonObject convertedObject = new Gson().fromJson(response.getBody(), JsonObject.class);
            rateLimit = new Gson().fromJson(convertedObject.getAsJsonObject("data"), KongConsumerGroupRatelimit.class);
        }catch (HttpClientErrorException httpClientErrorException){
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        } catch (Exception e){
            handledError.setErrorMsg(e.getMessage());
        }
        return rateLimit;
    }

    public int deleteRateLimitOnGroup(KongConsumerGroup group, KongConsumerGroupRatelimit rateLimit, HandledError handledError){
        int resInt = -1;
        String groupPath = getGroupPath(group);
        if (groupPath == null){
            handledError.setErrorMsg("Consumer group requires ID or Name.");
            return resInt;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.delete(kongManagementUrl+"/consumer_groups/"+groupPath+"/ratelimiting");
            resInt = 200;
        } catch (HttpClientErrorException httpClientErrorException){
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
            resInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e){
            resInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return resInt;
    }

    public int updateRateLimitOnGroup(KongConsumerGroup group, KongConsumerGroupRatelimit rateLimit, HandledError handledError){
        int resInt = -1;
        String groupPath = getGroupPath(group);
        if (groupPath == null){
            handledError.setErrorMsg("Consumer group requires ID or Name.");
            return resInt;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            RestTemplate restTemplate = new RestTemplate();
            MultiValueMap<String, Integer> data;
            data = new LinkedMultiValueMap<String, Integer>();
            if (rateLimit.getSecond() > 0){
                data.add("second", rateLimit.getSecond());
            }
            if (rateLimit.getMinute()>rateLimit.getSecond()){
                data.add("minute", rateLimit.getMinute());
            }
            if (rateLimit.getHour()>rateLimit.getMinute()){
                data.add("hour", rateLimit.getHour());
            }
            if (rateLimit.getDay()>rateLimit.getHour()){
                data.add("day", rateLimit.getDay());
            }
            if (rateLimit.getMonth()>rateLimit.getDay()) {
                data.add("month", rateLimit.getMonth());
            }
            if (rateLimit.getYear()>rateLimit.getYear()) {
                data.add("year", rateLimit.getYear());
            }
            HttpEntity<MultiValueMap<String, Integer>> request = new HttpEntity<MultiValueMap<String, Integer>>(data, headers);
            restTemplate.put(kongManagementUrl+"/consumer_groups/"+groupPath+"/ratelimiting", request);
            resInt = 200;
        } catch (HttpClientErrorException httpClientErrorException){
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
            resInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e){
            resInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return resInt;
    }

}
