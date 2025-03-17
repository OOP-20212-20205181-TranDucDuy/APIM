package com.company.apimanager.app;

import com.company.apimanager.app.kong_action.Cors;
import com.company.apimanager.app.kong_object.*;
import com.company.apimanager.app.kong_object.helper.*;
import com.company.apimanager.entity.BalancedHost;
import com.company.apimanager.entity.RestApi;
import com.company.apimanager.screen.consumer.KongRateLimitDetails;
import com.company.apimanager.screen.publishedproduct.HandledError;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vaadin.sass.internal.util.StringUtil;
import org.springframework.stereotype.Component;


import org.springframework.http.*;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;
import java.util.Arrays;
import java.util.List;


@Component
public class KongConnector {

    private static final String OAUTH_PLUGIN_NAME = "oauth2";

    private  static  final String OAUTH_SERVICE = "oauth2";

    private String kongManagementUrl;

    public KongConnector() {

    }

    public String getKongManagementUrl() {
        return kongManagementUrl;
    }

    public KongConnector(String kongManagementUrl) {
        this.kongManagementUrl = kongManagementUrl;
    }

    public int createOrUpdateService(KongNewServiceInfo kongService, KongServiceDetails kongServiceDetails
            , HandledError handledError) {
        int intRet = 0;
        int intService = this.createService(kongService, kongServiceDetails, handledError);
        if (intService != 201) {
            if (intService == 409) { //Service exist
                KongServiceDetails existKongServiceDetails = this.getServicebyName(kongService.getName(), handledError);
                if (existKongServiceDetails == null) {
                    intRet = -1;
                    handledError.setErrorMsg("Cloud not update service on API Gateway: " + handledError.getErrorMsg());
                } else {
                    try {
                        if ((kongService.getUrl() == null) && (kongService.getUpstreamPath() == null)) {
                            //Error
                            handledError.setErrorMsg("Cloud not update service on API Gateway: Service Host/path is NULL");
                            intRet = -1;
                        } else {
                            if (kongService.getUrl() != null) {
                                URI uri = new URI(kongService.getUrl());
                                existKongServiceDetails.setHost(uri.getHost());
                                existKongServiceDetails.setPath(uri.getPath());
                            } else {
                                existKongServiceDetails.setHost(kongService.getUpstreamName());
                                existKongServiceDetails.setPath(kongService.getUpstreamPath());
                            }
                            if (this.updateService(existKongServiceDetails, handledError) != 200) {
                                handledError.setErrorMsg("Cloud not update service on API Gateway: " + handledError.getErrorMsg());
                                intRet = -1;
                            }
                            kongServiceDetails.Assign(existKongServiceDetails);
                        }

                    } catch (Exception e) {
                        handledError.setErrorMsg(e.getMessage());
                        handledError.setErrorMsg("Could not update service on API Gateway: " + handledError.getErrorMsg());
                        intRet = -1;
                    }
                }
            } else {
                handledError.setErrorMsg("Cloud not create service on API Gateway: " + handledError.getErrorMsg());
                intRet = -1;
            }
        }
        return intRet;
    }

    public int createService(KongNewServiceInfo kongService, KongServiceDetails kongServiceDetails
            , HandledError handledError) {
        int retInt = -1;
        if (kongService == null) {
            handledError.setErrorMsg("Service info is Null");
            return retInt;
        }
        boolean urlNull = false;
        if ((Utility.CheckNullAndEmpty(kongService.getUrl())) &&
                (Utility.CheckNullAndEmpty(kongService.getUpstreamPath()))) {
            urlNull = true;
        }
        /* if ((kongService.getUrl() == null) && (kongService.getUpstreamPath() == null)) {
            urlNull = true;
        }
        if ((kongService.getName() == null) || (urlNull)) {
            handledError.setErrorMsg("Service info is Null");
            return retInt;
        }
         */
        if (Utility.CheckNullAndEmpty(kongService.getName()) || (urlNull)) {
            handledError.setErrorMsg("Service info is Null");
            return retInt;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            MultiValueMap<String, String> map;

            if (kongService.getUpstreamName() == null) {
                map = new LinkedMultiValueMap<String, String>();
                map.add("name", kongService.getName());
                map.add("url", kongService.getUrl());
            } else {
                map = new LinkedMultiValueMap<String, String>();
                map.add("name", kongService.getName());
                map.add("host", kongService.getUpstreamName());
                map.add("path", kongService.getUpstreamPath());
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
            String kongServiceUrl = kongManagementUrl + "/services";

            ResponseEntity<String> result = restTemplate.postForEntity(kongServiceUrl, request, String.class);
            retInt = result.getStatusCodeValue();

            JsonObject convertedObject = new Gson().fromJson(result.getBody(), JsonObject.class);
            String idString = convertedObject.get("id").getAsString();
            kongServiceDetails.setId(idString);
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

    public KongServiceDetails[] getServices() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<KongServicesResponse> response =
                restTemplate.getForEntity(
                        kongManagementUrl + "/services",
                        KongServicesResponse.class);
        return response.getBody().getData();
    }

    public KongServiceDetails getServicebyName(String serviceName, HandledError handledError) {
        KongServiceDetails kongServiceDetails = null;
        if (serviceName == null) {
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<KongServiceDetails> response =
                    restTemplate.getForEntity(
                            kongManagementUrl + "/services/" + serviceName,
                            KongServiceDetails.class);
            kongServiceDetails = response.getBody();
        } catch (HttpClientErrorException httpClientErrorException) {
            kongServiceDetails = null;
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        } catch (Exception e) {
            kongServiceDetails = null;
            handledError.setErrorMsg(e.getMessage());
        }
        return kongServiceDetails;
    }

    public int updateService(KongServiceDetails kongServiceDetails, HandledError handledError) {
        int retInt = 200;
        if (kongServiceDetails == null) {
            handledError.setErrorMsg("Kong info is Null");
            return -1;
        }
        if (kongServiceDetails.getId() == null) {
            handledError.setErrorMsg("Kong info is Null");
            return -1;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<KongServiceDetails> entity = new HttpEntity<KongServiceDetails>(kongServiceDetails, headers);
        RestTemplate restTemplate = new RestTemplate();

        if (kongServiceDetails.getId().isEmpty()) {
            handledError.setErrorMsg("Kong info is Null");
            return -1;
        }

        String putUrl = kongManagementUrl + "/services/" + kongServiceDetails.getId();

        try {
            restTemplate.exchange(putUrl, HttpMethod.PUT, entity, String.class).getBody();
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

    public int deleteWholeService(String serviceId, String routeId, String aclId, String groupName,
                                  HandledError handledError) {
        if (serviceId == null) {
            handledError.setErrorMsg("Service id is Null or Empty");
            return -1;
        }
        if (serviceId.isEmpty()) {
            handledError.setErrorMsg("Service id is Null or Empty");
            return -1;
        }

        KongAclDetails kongAclDetails = new KongAclDetails();
        //if (kongConnector.getAcl(servicesIdList[i], aclIdList[i], kongAclDetails, handledError) != 200) {
        boolean gotError = false;
        HandledError internalError = new HandledError();
        int intRet = 0;
        int getAclInt = this.getAcl(serviceId, aclId, kongAclDetails, internalError);
        if (getAclInt != 200) {
            if (getAclInt == 404) { //ACL not found

                this.deleteRoute(routeId, handledError);
                this.deleteService(serviceId, handledError);
            } else {
                gotError = true;
                handledError.setErrorMsg("Could not get ACL info for API from Gateway: " + internalError.getErrorMsg());
                intRet = -1;
            }
        } else {
            String[] groupList = kongAclDetails.getGroupList();
            if (groupList == null) {
                //Remove service, route
                this.deleteAcl(aclId, handledError);
                this.deleteRoute(routeId, handledError);
                this.deleteService(serviceId, handledError);
            } else {
                if (groupList.length == 0) {
                    //Remove service, route
                    this.deleteAcl(aclId, handledError);
                    this.deleteRoute(routeId, handledError);
                    this.deleteService(serviceId, handledError);
                } else {
                    String[] tmpGroupList = new String[groupList.length];
                    int n = 0;
                    boolean found = false;
                    for (int j = 0; j < groupList.length; j++) {
                        if (!groupList[j].equals(groupName)) {
                            tmpGroupList[n] = groupList[j];
                            n++;
                        } else {
                            found = true;
                        }
                    }
                    if (found) {
                        if (groupList.length == 1) {
                            this.deleteAcl(aclId, handledError);
                            this.deleteRoute(routeId, handledError);
                            this.deleteService(serviceId, handledError);
                        } else {
                            String[] newGroupList = new String[n];
                            for (int ind = 0; ind < n; ind++) {
                                newGroupList[ind] = tmpGroupList[ind];
                            }
                            kongAclDetails.setGroupList(newGroupList);
                            if (this.updateAcl(serviceId, aclId,
                                    kongAclDetails, handledError) != 200) {
                                gotError = true;

                                handledError.setErrorMsg("Could not get ACL info for API from Gateway: " + internalError.getErrorMsg());
                                intRet = -2;
                            }
                        }
                    } else {
                        //Error
                        handledError.setErrorMsg("That product does not subscribe API in Gateway");
                        gotError = true;
                        intRet = -3;
                    }
                }
            }
        }
        return intRet;
    }

    public int deleteService(String serviceId, HandledError handledError) {
        int retInt = -1;
        if (serviceId == null) {
            handledError.setErrorMsg("Service id is null or empty");
            return retInt;
        }
        if (serviceId.isEmpty()) {
            handledError.setErrorMsg("Service id is null or empty");
            return retInt;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String deleteUrl = kongManagementUrl + "/services/" + serviceId;

            HttpHeaders headers = new HttpHeaders();

            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<KongServiceDetails> entity = new HttpEntity<KongServiceDetails>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);
            retInt = response.getStatusCodeValue();
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            handledError.setErrorMsg(httpClientErrorException.getMessage());
        } catch (Exception e) {
            retInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return retInt; //204 succesfull
    }

    public int createOrUpdateRoute(KongNewRouteInfo kongNewRouteInfo, KongRouteDetails kongRouteDetails
            , HandledError handledError) {
        int intRet = 0;
        int intRoute = this.createRoute(kongNewRouteInfo, kongRouteDetails, handledError);
        if (intRoute == 201) {
            return intRet;
        }
        if (intRoute != 409) {
            handledError.setErrorMsg("Could not create route on API Gateway: " + handledError.getErrorMsg());
            return -1;
        }
        KongRouteDetails existKongRouteDetails = this.getRoutebyName(kongNewRouteInfo.getName(), handledError);
        if (existKongRouteDetails == null) {
            handledError.setErrorMsg("Could not update route on API Gateway: " + handledError.getErrorMsg());
            return -1;
        }
        kongRouteDetails.Assign(existKongRouteDetails);
        return intRet;
    }

    public int createRoute(KongNewRouteInfo kongNewRouteInfo, KongRouteDetails kongRouteDetails
            , HandledError handledError) {
        int retInt = -1;

        if (kongNewRouteInfo == null) {
            handledError.setErrorMsg("New route info is Null");
            return retInt;
        }
        if ((kongNewRouteInfo.getServiceName() == null) || (kongNewRouteInfo.getName() == null)) {
            handledError.setErrorMsg("New route name or service name is Null");
            return retInt;
        }
        if ((kongNewRouteInfo.getServiceName().isEmpty()) ||
                (kongNewRouteInfo.getName().isEmpty())) {
            handledError.setErrorMsg("New route name or service name is empty");
            return retInt;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            List<String> paths = Arrays.asList(kongNewRouteInfo.getPath());
            map.addAll("paths", paths);
            map.add("name", kongNewRouteInfo.getName());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
            String kongServiceUrl = kongManagementUrl + "/services/" + kongNewRouteInfo.getServiceName() + "/routes";

            ResponseEntity<String> result = restTemplate.postForEntity(kongServiceUrl, request, String.class);
            retInt = result.getStatusCodeValue();
            JsonObject convertedObject = new Gson().fromJson(result.getBody(), JsonObject.class);
            String idString = convertedObject.get("id").getAsString();
            kongRouteDetails.setId(idString);
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

    public int deleteRoute(String routeId, HandledError handledError) {
        int retInt = -1;
        if (routeId == null) {
            handledError.setErrorMsg("Route id is null or empty");
            return retInt;
        }
        if (routeId.isEmpty()) {
            handledError.setErrorMsg("Route id is null or empty");
            return retInt;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String deleteUrl = kongManagementUrl + "/routes/" + routeId;

            HttpHeaders headers = new HttpHeaders();

            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<KongServiceDetails> entity = new HttpEntity<KongServiceDetails>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);
            retInt = response.getStatusCodeValue();
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            handledError.setErrorMsg(httpClientErrorException.getMessage());
        } catch (Exception e) {
            retInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return retInt; //204 sucessfull
    }

    public KongRouteDetails getRoutebyName(String routeName, HandledError handledError) {
        KongRouteDetails kongRouteDetails = null;
        if (routeName == null) {
            return kongRouteDetails;
        }
        if (routeName.isEmpty()) {
            return kongRouteDetails;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<KongRouteDetails> response =
                    restTemplate.getForEntity(
                            kongManagementUrl + "/routes/" + routeName,
                            KongRouteDetails.class);
            kongRouteDetails = response.getBody();
        } catch (HttpClientErrorException httpClientErrorException) {
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        } catch (Exception e) {
            kongRouteDetails = null;
            handledError.setErrorMsg(e.getMessage());
        }
        return kongRouteDetails;
    }

    public int createOrGetExistingConsumer(String username, KongConsumerDetails kongConsumerDetails
            , HandledError handledError) {
        int createRet = this.createConsumer(username, kongConsumerDetails, handledError);
        if (createRet == 201) {
            return 0;
        }
        if (createRet != 409) {
            return -1;
        }
        if (this.getConsumerByName(username, kongConsumerDetails, handledError) != 200) {
            return -2;
        }
        return 0;
    }

    public int getConsumerByName(String username, KongConsumerDetails kongConsumerDetails
            , HandledError handledError) {
        if (username == null) {
            handledError.setErrorMsg("Consumer name is NULL");
            return -1;
        }
        if (username.isEmpty()) {
            handledError.setErrorMsg("Consumer name is Empty");
            return -1;
        }

        int retInt = 200;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response =
                    restTemplate.getForEntity(
                            kongManagementUrl + "/consumers/" + username,
                            String.class);
            JsonObject convertedObject = new Gson().fromJson(response.getBody(), JsonObject.class);
            kongConsumerDetails.setId(convertedObject.get("id").getAsString());
        } catch (HttpClientErrorException httpClientErrorException) {
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
            retInt = -2;
        } catch (Exception e) {
            retInt = -2;
            handledError.setErrorMsg(e.getMessage());
        }
        return retInt;
    }

    public int createConsumer(String username, KongConsumerDetails kongConsumerDetails
            , HandledError handledError) {
        int retInt = -1;
        if (username == null) {
            handledError.setErrorMsg("Username is Null");
            return retInt;
        }
        if (username.isEmpty()) {
            handledError.setErrorMsg("Username is Null");
            return retInt;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("username", username);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
            String kongUrl = kongManagementUrl + "/consumers";

            ResponseEntity<String> result = restTemplate.postForEntity(kongUrl, request, String.class);
            retInt = result.getStatusCodeValue();

            JsonObject convertedObject = new Gson().fromJson(result.getBody(), JsonObject.class);
            String idString = convertedObject.get("id").getAsString();
            kongConsumerDetails.setId(idString);
        } catch (HttpClientErrorException httpClientErrorException) {
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -100;
        }
        return retInt; //201 Successfull
    }

    public int createOrUpdateAcl(String serviceName, String groupName,
                                 KongAclDetails kongAclDetails, HandledError handledError) {
        if ((serviceName == null) || (groupName == null)) {
            handledError.setErrorMsg("Service name or group name is Null");
            return -1;
        }
        if ((serviceName.isEmpty()) || (groupName.isEmpty())) {
            handledError.setErrorMsg("Service name or group name is Empty");
            return -1;
        }

        KongAclDetails existKongAclDetails = new KongAclDetails();
        int aclInt = this.getAclFromService(serviceName, existKongAclDetails, handledError);
        int intRet = 0;

        if (aclInt == 200) {
            String[] groupList = existKongAclDetails.getGroupList();
            if (groupList == null) {
                String[] newGroupList = new String[1];
                newGroupList[0] = groupName;
                existKongAclDetails.setGroupList(newGroupList);
                if (this.updateAcl(serviceName, existKongAclDetails.getId(), existKongAclDetails,
                        handledError) != 200) {
                    handledError.setErrorMsg("Could not update the service ACL in Kong Gateway:" +
                            handledError.getErrorMsg());
                    intRet = -1;
                } else {
                    kongAclDetails.setId(existKongAclDetails.getId());
                    kongAclDetails.setGroupList(newGroupList);
                }
            } else {
                boolean grpExist = false;
                for (int i = 0; i < groupList.length; i++) {
                    if (groupList[i].equals(groupName)) {
                        grpExist = true;
                        break;
                    }
                }

                if (!grpExist) {
                    String[] newGroupList = new String[groupList.length + 1];
                    for (int i = 0; i < groupList.length; i++) {
                        newGroupList[i] = groupList[i];
                    }
                    newGroupList[newGroupList.length - 1] = groupName;
                    existKongAclDetails.setGroupList(newGroupList);
                    if (this.updateAcl(serviceName, existKongAclDetails.getId(), existKongAclDetails,
                            handledError) != 200) {
                        handledError.setErrorMsg("Could not update the service ACL in Kong Gateway:" +
                                handledError.getErrorMsg());
                        intRet = -1;
                    } else {
                        kongAclDetails.setId(existKongAclDetails.getId());
                        kongAclDetails.setGroupList(newGroupList);
                    }
                } else {
                    kongAclDetails.setId(existKongAclDetails.getId());
                    kongAclDetails.setGroupList(groupList);
                }
            }
        } else {
            String[] groupList = new String[1];
            groupList[0] = groupName;
            int createRet = this.createAcl(serviceName, groupList,
                    kongAclDetails, handledError);
            if (createRet != 201) {
                handledError.setErrorMsg("Could not create the service ACL in Kong Gateway:" +
                        handledError.getErrorMsg());
                intRet = -1;
            }
        }
        return intRet;
    }

    public int createAcl(String serviceName, String[] groupList,
                         KongAclDetails kongAclDetails, HandledError handledError) {
        int retInt = 201;
        if (serviceName == null) {
            handledError.setErrorMsg("Username is Null");
            return -1;
        }
        if (serviceName.isEmpty()) {
            handledError.setErrorMsg("Username is Empty");
            return -1;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            String postStr = "{\n" +
                    "    \"name\":\"acl\",\n" +
                    "    \"config\":\n" +
                    "    {\n" +
                    "        \"allow\":[\"";
            String groupStr = groupList[0] + "\"";
            for (int i = 1; i < groupList.length; i++) {
                groupStr = groupStr + ",\"" + groupList[i] + "\"";
            }
            postStr = postStr + groupStr + "], \n" +
                    "        \"hide_groups_header\":true\n" +
                    "    }\n" +
                    "}";
            HttpEntity<String> request =
                    new HttpEntity<String>(postStr, headers);
            String kongServiceUrl = kongManagementUrl + "/services/" + serviceName + "/plugins";

            String resultAsJsonStr =
                    restTemplate.postForObject(kongServiceUrl, request, String.class);
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            String idString = convertedObject.get("id").getAsString();
            kongAclDetails.setId(idString);
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

    public int getAclFromService(String serviceName, KongAclDetails kongAclDetails,
                                 HandledError handledError) {
        int retInt = 200;
        if (serviceName == null) {
            handledError.setErrorMsg("Service name is Null");
            return -1;
        }
        if (serviceName.isEmpty()) {
            handledError.setErrorMsg("Service name is Empty");
            return -1;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            String kongServiceUrl = kongManagementUrl + "/services/" + serviceName
                    + "/plugins";
            String resultAsJsonStr = restTemplate.exchange(kongServiceUrl, HttpMethod.GET, entity,
                    String.class).getBody();
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            JsonArray jsonArray = convertedObject.get("data").getAsJsonArray();
            //String[] tmpList = new String[jsonArray.size()];
            if (jsonArray.size() == 0) {
                retInt = 404;
            } else {
                boolean foundAcl = false;
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject pluginObject = jsonArray.get(i).getAsJsonObject();
                    if (pluginObject.get("name").getAsString().equals("acl")) {
                        kongAclDetails.setId(pluginObject.get("id").getAsString());
                        JsonArray groupArray = pluginObject.getAsJsonObject("config").get("allow").getAsJsonArray();
                        String[] tmpList = new String[groupArray.size()];
                        for (int j = 0; j < groupArray.size(); j++) {
                            tmpList[j] = groupArray.get(j).getAsString();
                        }
                        kongAclDetails.setGroupList(tmpList);
                        foundAcl = true;
                        break;
                    }
                }
                if (!foundAcl) {
                    retInt = 404;
                }
            }
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

    public int getAcl(String serviceName, String aclId, KongAclDetails kongAclDetails,
                      HandledError handledError) {
        int retInt = 200;
        if ((serviceName == null) || (aclId == null)) {
            handledError.setErrorMsg("Service name is Null");
            return -1;
        }
        if ((serviceName.isEmpty()) || (aclId.isEmpty())) {
            handledError.setErrorMsg("Service name is Empty");
            return -1;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            String kongServiceUrl = kongManagementUrl + "/services/" + serviceName
                    + "/plugins/" + aclId;
            String resultAsJsonStr = restTemplate.exchange(kongServiceUrl, HttpMethod.GET, entity,
                    String.class).getBody();
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            JsonObject configObject = convertedObject.getAsJsonObject("config");
            JsonArray jsonArray = configObject.get("allow").getAsJsonArray();
            String[] tmpList = new String[jsonArray.size()];
            if (jsonArray.size() == 0) {
                retInt = 404;
            } else {
                for (int i = 0; i < jsonArray.size(); i++) {
                    tmpList[i] = jsonArray.get(i).getAsString();
                }
                kongAclDetails.setId(aclId);
                kongAclDetails.setGroupList(tmpList);
            }
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

    public int updateAcl(String serviceName, String aclId, KongAclDetails kongAclDetails,
                         HandledError handledError) {
        int retInt = 200;
        if ((serviceName == null) || (aclId == null)) {
            handledError.setErrorMsg("Service name or ACL id is Null");
            return -1;
        }
        if ((serviceName.isEmpty()) || (aclId.isEmpty())) {
            handledError.setErrorMsg("Service name  or ACL id is Empty");
            return -1;
        }

        if (kongAclDetails == null) {
            handledError.setErrorMsg("No group info");
            return -1;
        } else {
            if (kongAclDetails.getGroupList() == null) {
                handledError.setErrorMsg("No group info");
                return -1;
            } else {
                if (kongAclDetails.getGroupList().length == 0) {
                    handledError.setErrorMsg("No group info");
                    return -1;
                }
            }
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        String putStr = "{\n" +
                "    \"config\": {\n" +
                "        \"allow\": [\n";
        int lengthInt = kongAclDetails.getGroupList().length;
        for (int i = 0; i < lengthInt - 1; i++) {
            putStr = putStr + "\"" + kongAclDetails.getGroupList()[i] + "\",\n";
        }
        putStr = putStr + "\"" + kongAclDetails.getGroupList()[lengthInt - 1] + "\"\n";
        putStr = putStr +
                "        ],\n" +
                "        \"hide_groups_header\": true,\n" +
                "        \"deny\": null\n" +
                "    },\n" +
                "    \"name\": \"acl\",\n" +
                "    \"protocols\": [\n" +
                "        \"grpc\",\n" +
                "        \"grpcs\",\n" +
                "        \"http\",\n" +
                "        \"https\"\n" +
                "    ],\n" +
                "    \"tags\": null,    \n" +
                "    \"enabled\": true,\n" +
                "    \"route\": null,\n" +
                "\n" +
                "    \"consumer\": null\n" +
                "}";
        HttpEntity<String> entity = new HttpEntity<String>(putStr, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            String kongServiceUrl = kongManagementUrl + "/services/" + serviceName
                    + "/plugins/" + aclId;
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

    public int deletePlugin(String pluginId, HandledError handledError) {
        int retInt = -1;
        if (pluginId == null) {
            handledError.setErrorMsg("Plugin id is Null");
            return -1;
        }
        if (pluginId.isEmpty()) {
            handledError.setErrorMsg("Plugin id is Empty");
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

    public int deleteAcl(String aclId, HandledError handledError) {
        int retInt = -1;
        if (aclId == null) {
            handledError.setErrorMsg("Acl id is Null");
            return -1;
        }
        if (aclId.isEmpty()) {
            handledError.setErrorMsg("Acl id is Empty");
            return -1;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String deleteUrl = kongManagementUrl + "/plugins/" + aclId;

            HttpHeaders headers = new HttpHeaders();

            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<KongServiceDetails> entity = new HttpEntity<KongServiceDetails>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);
            retInt = response.getStatusCodeValue();
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            handledError.setErrorMsg(httpClientErrorException.getMessage());
        } catch (Exception e) {
            retInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return retInt;
    }

    public int deleteConsumer(String consumerId, HandledError handledError) {
        int retInt = -1;
        if (consumerId == null) {
            handledError.setErrorMsg("Consumer id is Null");
            return retInt;
        }
        if (consumerId.isEmpty()) {
            handledError.setErrorMsg("Consumer id is Null");
            return retInt;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String deleteUrl = kongManagementUrl + "/consumers/" + consumerId;

            HttpHeaders headers = new HttpHeaders();

            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<KongServiceDetails> entity = new HttpEntity<KongServiceDetails>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);
            retInt = response.getStatusCodeValue();
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            retInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return retInt;
    }

    public int createOrUpdateRateLimit(String consumerName, KongRateLimitInfo kongRateLimitInfo,
                                       KongRateLimitDetails kongRateLimitDetails,
                                       HandledError handledError) {
        int createRet = createRateLimit(consumerName, kongRateLimitInfo, kongRateLimitDetails, handledError);
        if (createRet == 201) {
            return 0;
        }
        if (createRet != 409) {
            return -1;
        }
        String existingId = this.getConsumerRateLimitPlugin(consumerName, handledError);
        if (existingId == null) {
            return -1;
        }
        if (this.updateRateLimit(consumerName, existingId, kongRateLimitInfo, handledError) != 200) {
            return -1;
        }
        kongRateLimitDetails.setId(existingId);
        return 0;
    }

    public int createRateLimit(String consumerName, KongRateLimitInfo kongRateLimitInfo,
                               KongRateLimitDetails kongRateLimitDetails, HandledError handledError) {
        int retInt = 201;
        if (consumerName == null) {
            handledError.setErrorMsg("Consumer name is Null");
            return -1;
        }
        if (consumerName.isEmpty()) {
            handledError.setErrorMsg("Consumer name is empty");
            return -1;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            String postStr = "{\n" +
                    "    \"name\":\"rate-limiting\",\n" +
                    "    \"config\":\n" +
                    "    {\n" +
                    "\"second\": " + kongRateLimitInfo.getMax_per_second() +
                    ",\"minute\": " + kongRateLimitInfo.getMax_per_minute() +
                    ",\"hour\": " + kongRateLimitInfo.getMax_per_hour() +
                    ",\"policy\": \"local\",\n" +
                    "\"fault_tolerant\":true,\n" +
                    "\"hide_client_headers\":false,\n" +
                    "\"redis_ssl\":false,\n" +
                    "\"redis_ssl_verify\":false" +
                    "}\n" +
                    "}";
            HttpEntity<String> request =
                    new HttpEntity<String>(postStr, headers);
            String kongServiceUrl = kongManagementUrl + "/consumers/" + consumerName + "/plugins";

            String resultAsJsonStr =
                    restTemplate.postForObject(kongServiceUrl, request, String.class);
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            String idString = convertedObject.get("id").getAsString();
            kongRateLimitDetails.setId(idString);
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

    public int updateRateLimit(String consumerName, String limitPluginId,
                               KongRateLimitInfo kongRateLimitInfo, HandledError handledError) {
        int retInt = 200;
        if ((consumerName == null) || (limitPluginId == null)) {
            handledError.setErrorMsg("Consumer name or Limit id is Null");
            return -1;
        }
        if ((consumerName.isEmpty()) || (limitPluginId.isEmpty())) {
            handledError.setErrorMsg("Consumer name or Limit id is empty");
            return -1;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        String secondStr = "null";
        String minuteStr = "null";
        String hourStr = "null";
        if (kongRateLimitInfo.getMax_per_second() > 0) {
            secondStr = kongRateLimitInfo.getMax_per_second() + "";
        }
        if (kongRateLimitInfo.getMax_per_minute() > 0) {
            minuteStr = kongRateLimitInfo.getMax_per_minute() + "";
        }
        if (kongRateLimitInfo.getMax_per_hour() > 0) {
            hourStr = kongRateLimitInfo.getMax_per_hour() + "";
        }
        String putStr = "{\n" +
                "    \"config\": {\n" +
                "        \"redis_password\": null,\n" +
                "        \"redis_username\": null,\n" +
                "        \"limit_by\": \"consumer\",\n" +
                "        \"header_name\": null,\n" +
                "        \"second\": " + secondStr + ",\n" +
                "        \"minute\": " + minuteStr + ",\n" +
                "        \"hour\": " + hourStr + ",\n" +
                "        \"day\": null,\n" +
                "        \"month\": null,\n" +
                "        \"year\": null,\n" +
                "        \"hide_client_headers\": false,\n" +
                "        \"policy\": \"local\",\n" +
                "        \"fault_tolerant\": true,\n" +
                "        \"redis_timeout\": 2000,\n" +
                "        \"redis_ssl\": false,\n" +
                "        \"redis_ssl_verify\": false,\n" +
                "        \"redis_server_name\": null,\n" +
                "        \"redis_database\": 0,\n" +
                "        \"redis_host\": null,\n" +
                "        \"redis_port\": 6379,\n" +
                "        \"path\": null\n" +
                "    },\n" +
                "    \"name\": \"rate-limiting\",\n" +
                "    \"protocols\": [\n" +
                "        \"grpc\",\n" +
                "        \"grpcs\",\n" +
                "        \"http\",\n" +
                "        \"https\"\n" +
                "    ],\n" +
                "    \"tags\": null,\n" +
                "    \"service\": null,\n" +
                "    \"created_at\": 1652089128,\n" +
                "    \"enabled\": true,\n" +
                "    \"route\": null\n" +
                "    }\n";
        //+ "}";

        HttpEntity<String> entity = new HttpEntity<String>(putStr, headers);
        RestTemplate restTemplate = new RestTemplate();

        String putUrl = kongManagementUrl + "/consumers/" + consumerName + "/plugins/"
                + limitPluginId;
        //String putUrl = "http://localhost:8000/bidv/Closed/Customer/1";
        try {
            String resutlStr = restTemplate.exchange(putUrl, HttpMethod.PUT, entity, String.class).getBody();
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

    public String getConsumerRateLimitPlugin(String consumerName, HandledError handledError) {
        KongConsumerPlugin[] kongConsumerPlugins = getConsumerPlugins(consumerName, handledError);
        if (kongConsumerPlugins == null) {
            return null;
        }
        String pluginId = null;
        for (int i = 0; i < kongConsumerPlugins.length; i++) {
            if (kongConsumerPlugins[i].getName().equals("rate-limiting")) {
                pluginId = kongConsumerPlugins[i].getKongId();
                break;
            }
        }
        return pluginId;
    }

    public KongConsumerPlugin[] getConsumerPlugins(String consumerName, HandledError handledError) {
        if (consumerName == null) {
            handledError.setErrorMsg("Consumer name is Null");
            return null;
        }
        if (consumerName.isEmpty()) {
            handledError.setErrorMsg("Consumer name is Empty");
            return null;
        }
        KongConsumerPlugin[] kongConsumerPlugins = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response =
                    restTemplate.getForEntity(
                            kongManagementUrl + "/consumers/" + consumerName + "/plugins",
                            String.class);
            JsonObject convertedObject = new Gson().fromJson(response.getBody(), JsonObject.class);
            JsonArray jsonArray = convertedObject.get("data").getAsJsonArray();
            if (jsonArray.size() > 0) {
                kongConsumerPlugins = new KongConsumerPlugin[jsonArray.size()];
                for (int i = 0; i < jsonArray.size(); i++) {
                    String idStr = jsonArray.get(i).getAsJsonObject().get("id").getAsString();
                    String nameStr = jsonArray.get(i).getAsJsonObject().get("name").getAsString();
                    kongConsumerPlugins[i] = new KongConsumerPlugin();
                    kongConsumerPlugins[i].setKongId(idStr);
                    kongConsumerPlugins[i].setName(nameStr);
                }
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            kongConsumerPlugins = null;
        } catch (Exception e) {
            kongConsumerPlugins = null;
        }
        return kongConsumerPlugins;

    }

    public int joinConsumerToGroup(String consumerName, String groupName,
                                   KongConsumerGroupDetails kongConsumerGroupDetails,
                                   HandledError handledError) {
        int retInt = 201;
        if ((consumerName == null) || (groupName == null)) {
            handledError.setErrorMsg("Consumer name or group name is Null");
            return -1;
        }
        if ((consumerName.isEmpty()) || (groupName.isEmpty())) {
            handledError.setErrorMsg("Consumer name or group name is Null");
            return -1;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            String postStr = "{\n" +
                    "    \"group\":\"" + groupName
                    + "\"\n" + "}";
            HttpEntity<String> request =
                    new HttpEntity<String>(postStr, headers);
            String kongServiceUrl = kongManagementUrl + "/consumers/" + consumerName + "/acls";

            String resultAsJsonStr =
                    restTemplate.postForObject(kongServiceUrl, request, String.class);
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            String idString = convertedObject.get("id").getAsString();
            kongConsumerGroupDetails.setId(idString);
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

    public int getConsumerGroupInfo(String consumerName, String groupName,
                                    KongConsumerGroupDetails kongConsumerGroupDetails,
                                    HandledError handledError) {
        int retInt = 200;
        if ((consumerName == null) || (groupName == null)) {
            handledError.setErrorMsg("Consumer name or group name is Null");
            return -1;
        }
        if ((consumerName.isEmpty()) || (groupName.isEmpty())) {
            handledError.setErrorMsg("Consumer name or group name is Null");
            return -1;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        RestTemplate restTemplate = new RestTemplate();
        boolean found = false;
        try {
            String kongServiceUrl = kongManagementUrl + "/consumers/" + consumerName
                    + "/acls";
            String resultAsJsonStr = restTemplate.exchange(kongServiceUrl, HttpMethod.GET, entity,
                    String.class).getBody();
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            JsonArray jsonArray = convertedObject.get("data").getAsJsonArray();
            if (jsonArray.size() == 0) {
                retInt = 404;
            } else {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject groupObject = jsonArray.get(i).getAsJsonObject();
                    String strId = groupObject.get("id").getAsString();
                    String strGroup = groupObject.get("group").getAsString();
                    if (strGroup.equals(groupName)) {
                        found = true;
                        kongConsumerGroupDetails.setId(strId);
                        kongConsumerGroupDetails.setGroupName(groupName);
                        break;
                    }
                }
                if (!found) {
                    retInt = 404;
                }
            }
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

    public int unJoinConsumerFromGroup(String consumerName, String consumerGroupId,
                                       HandledError handledError) {
        int retInt = -1;
        if ((consumerName == null) || (consumerGroupId == null)) {
            handledError.setErrorMsg("Consumer name or group id is Null");
            return -1;
        }
        if ((consumerName.isEmpty()) || (consumerGroupId.isEmpty())) {
            handledError.setErrorMsg("Consumer name or group id is Null");
            return -1;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String deleteUrl = kongManagementUrl + "/consumers/" + consumerName
                    + "/acls/" + consumerGroupId;

            HttpHeaders headers = new HttpHeaders();

            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<KongServiceDetails> entity = new HttpEntity<KongServiceDetails>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);
            retInt = response.getStatusCodeValue();
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            retInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return retInt;
    }

    public int enableOrUpdateServiceBasicAuth(String serviceId, HandledError handledError) {
        int enableRet = this.enableServiceBasicAuth(serviceId, handledError);
        if (enableRet == 201) {
            return 0;
        }
        if (enableRet != 409) {
            return -1;
        }
        KongServicePlugin kongServicePlugin = this.getServicePluginByType(serviceId, "basic-auth",
                handledError);
        if (kongServicePlugin == null) {
            return -1;
        }
        return 0;
    }

    public int enableServiceBasicAuth(String serviceId, HandledError handledError) {
        int retInt = 201;
        if (serviceId == null) {
            handledError.setErrorMsg("Service id/name is empty");
            return -1;
        }
        if (serviceId.isEmpty()) {
            handledError.setErrorMsg("Service id/name is empty");
            return -1;
        }

        String resultAsJsonStr = "";
        try {
            String kongServiceUrl = kongManagementUrl + "/services/" + serviceId + "/plugins";
            String body = "{\"name\": \"basic-auth\"}";

            RestClient restTemplate = new RestClient();
            resultAsJsonStr = restTemplate.post(kongServiceUrl, body);
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
        }
        return retInt;
    }

    public int enableRouteBasicAuth(String routeId, HandledError handledError) {
        int retInt = 201;
        if (routeId == null) {
            handledError.setErrorMsg("Service id/name is empty");
            return -1;
        }
        if (routeId.isEmpty()) {
            handledError.setErrorMsg("Service id/name is empty");
            return -1;
        }

        try {
            String kongServiceUrl = kongManagementUrl + "/routes/" + routeId + "/plugins";
            RestClient restTemplate = new RestClient();
            String body = "{\"name\": \"basic-auth\"}";

            String resultAsJsonStr = restTemplate.post(kongServiceUrl, body);
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -2;
        }
        return retInt;
    }

    public int enableOrUpdateServiceJwtAuth(String serviceId, HandledError handledError) {
        int enableRet = this.enableServiceJwtAuth(serviceId, handledError);
        if (enableRet == 201) {
            return 0;
        }
        if (enableRet != 409) {
            return -1;
        }
        KongServicePlugin kongServicePlugin = this.getServicePluginByType(serviceId, "jwt",
                handledError);
        if (kongServicePlugin == null) {
            return -1;
        }
        return 0;
    }

    public int enableServiceJwtAuth(String serviceId, HandledError handledError) {
        int retInt = 201;
        if (serviceId == null) {
            handledError.setErrorMsg("Service id/name is empty");
            return -1;
        }
        if (serviceId.isEmpty()) {
            handledError.setErrorMsg("Service id/name is empty");
            return -1;
        }

        String resultAsJsonStr = "";
        try {
            String kongServiceUrl = kongManagementUrl + "/services/" + serviceId + "/plugins";
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            JwtAuthenticationPlugin jwt = new JwtAuthenticationPlugin(new JwtAuthenticationPluginConfig());
            String body = objectMapper.writeValueAsString(jwt);
            RestClient restTemplate = new RestClient();
            resultAsJsonStr = restTemplate.post(kongServiceUrl, body);
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
        }
        return retInt;
    }

    public int enableRouteJwtAuth(String routeId, HandledError handledError) {
        //todo
        return -1;
    }

    public int enableOrUpdateServiceApiKeyAuth(String serviceName, String keyName, HandledError handledError) {
        int enableRet = this.enableServiceApiKeyAuth(serviceName, keyName, handledError);
        if (enableRet == 201) {
            return 0;
        }
        if (enableRet != 409) {
            return -1;
        }
        KongServicePlugin kongServicePlugin = this.getServicePluginByType(serviceName, "key-auth",
                handledError);
        if (kongServicePlugin == null) {
            return -1;
        }
        return 0;
    }

    public int enableServiceApiKeyAuth(String serviceName, String keyName, HandledError handledError) {
        int retInt = 201;
        if ((serviceName == null) || (keyName == null)) {
            handledError.setErrorMsg("Service name or key name is Null");
            return -1;
        }
        if ((serviceName.isEmpty()) || (keyName.isEmpty())) {
            handledError.setErrorMsg("Service name or key name is Empty");
            return -1;
        }

        try {
            String kongServiceUrl = kongManagementUrl + "/services/" + serviceName + "/plugins";
            RestClient restTemplate = new RestClient();
            String body = String.format("{" +
                    "    \"name\": \"key-auth\"," +
                    "    \"config\": {" +
                    "        \"key_names\": [\"%s\"]," +
                    "        \"key_in_body\": true," +
                    "        \"key_in_header\": true," +
                    "        \"key_in_query\": true," +
                    "        \"hide_credentials\": false," +
                    "        \"run_on_preflight\": true" +
                    "    }" +
                    "}", keyName);

            String resultAsJsonStr = restTemplate.post(kongServiceUrl, body);
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -2;
        }
        return retInt;
    }

    public KongServiceOAuth enableOrUpdateServiceOAuth(String serviceName, String[] scopes, int grantType,
                                                       HandledError handledError) {
        HandledError createError = new HandledError();
        KongServiceOAuth kongServiceOAuth = new KongServiceOAuth();
        int enableRet = enableServiceOAuth(serviceName, scopes, grantType, kongServiceOAuth, createError);
        if (enableRet == 201) {
            return kongServiceOAuth;
        }
        if (enableRet != 409) {
            handledError.Assign(createError);
            return null;
        }
        KongServicePlugin kongServicePlugin = this.getServicePluginByType(serviceName, "oauth2",
                handledError);
        if (kongServicePlugin == null) {
            return null;
        }

        /*
        if (this.deletePlugin(kongServicePlugin.getKongId(), handledError) != 204) {
            handledError.Assign(createError);
            return null;
        }
        if (this.enableServiceOAuth(serviceName, scopes, kongServiceOAuth, createError) != 201) {
            handledError.Assign(createError);
            return null;
        }
         */
        kongServiceOAuth.setKongId(kongServicePlugin.getKongId());
        kongServiceOAuth.setProvisionKey(kongServicePlugin.getOauthProvisionKey());
        return kongServiceOAuth;
    }

    public int enableServiceOAuth(String serviceName, String[] scopes, int grantType,
                                  KongServiceOAuth kongServiceOAuth, HandledError handledError) {
        if ((serviceName == null) || (scopes == null)) {
            handledError.setErrorMsg("Service name or scope is Null");
            return -1;
        }
        if (serviceName.isEmpty()) {
            handledError.setErrorMsg("Service name is Empty");
            return -1;
        }
        if (scopes.length == 0) {
            handledError.setErrorMsg("Scope is Empty");
            return -1;
        }
        int retInt = 201;
        try {
            String kongServiceUrl = kongManagementUrl + "/services/" + serviceName + "/plugins";
            RestClient restTemplate = new RestClient();
            String bodyBegin = "{\n" +
                    "    \"name\":\"oauth2\",\n" +
                    "    \"config\":\n" +
                    "    {\n";
            //String bodyMiddle = "        \"scopes\":[\"email\",\"phone\"],\n";
            String bodyMiddle = "        \"scopes\":[";
            for (int i = 0; i < scopes.length - 1; i++) {
                bodyMiddle = bodyMiddle + "\"" + scopes[i] + "\",";
            }
            bodyMiddle = bodyMiddle + "\"" + scopes[scopes.length - 1] + "\"],\n";
            bodyMiddle = bodyMiddle +
                    "        \"accept_http_if_already_terminated\":true,\n";
            String grantString;
            if (grantType == 1) {   //Authorization code grant type
                grantString = "        \"enable_authorization_code\":true,\n";
            }
            else {
                grantString = "        \"enable_client_credentials\":true,\n";
            }

            String bodyEnd =
                    "        \"mandatory_scope\":true,\n" +
                            grantString +
                            "        \"token_expiration\":7200\n" +
                            "    }\n" +
                            "}";
            String resultAsJsonStr = restTemplate.post(kongServiceUrl, bodyBegin + bodyMiddle + bodyEnd);
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            kongServiceOAuth.setKongId(convertedObject.get("id").getAsString());
            if (grantType == 1) {
                kongServiceOAuth.setProvisionKey(convertedObject.get("config").
                        getAsJsonObject().get("provision_key").getAsString());
            }
            else {
                kongServiceOAuth.setProvisionKey("");
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -2;
        }
        return retInt;
    }

    public int deleteServicePlugin(String serviceName, String pluginId, HandledError handledError) {
        int retInt;

        if ((serviceName == null) || (pluginId == null)){
            handledError.setErrorMsg("Service name is Null");
            return -1;
        }
        if ((serviceName.isEmpty()) || (pluginId.isEmpty())) {
            handledError.setErrorMsg("Service name is Empty");
            return -1;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String deleteUrl = kongManagementUrl + "/services/" + serviceName + "/plugins/" + pluginId;

            HttpHeaders headers = new HttpHeaders();

            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<KongServiceDetails> entity = new HttpEntity<KongServiceDetails>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);
            retInt = response.getStatusCodeValue();
        }
        catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            handledError.setErrorMsg(httpClientErrorException.getMessage());
        }
        catch (Exception e) {
            retInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return retInt;
    }

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

    public int enableRouteApiKeyAuth(String serviceId, String keyName, HandledError handledError) {
        int retInt = 201;
        if ((serviceId == null) || (keyName == null)) {
            handledError.setErrorMsg("Route id or key name is Null");
            return -1;
        }
        if ((serviceId.isEmpty()) || (keyName.isEmpty())) {
            handledError.setErrorMsg("Route id or key name is Empty");
            return -1;
        }

        try {
            String kongRouteUrl = kongManagementUrl + "/routes/" + serviceId + "/plugins";
            RestClient restTemplate = new RestClient();
            String body = String.format("{" +
                    "    \"name\": \"key-auth\"," +
                    "    \"config\": {" +
                    "        \"key_names\": [\"%s\"]," +
                    "        \"key_in_body\": false," +
                    "        \"key_in_header\": true," +
                    "        \"key_in_query\": true," +
                    "        \"hide_credentials\": false," +
                    "        \"run_on_preflight\": true" +
                    "    }" +
                    "}", keyName);

            String resultAsJsonStr = restTemplate.post(kongRouteUrl, body);
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -2;
        }
        return retInt;
    }

    public int createOrUpdateBasicCredentialForConsumer(String consumerName, String userName,
                                                        String password, StringBuilder idStr,
                                                        HandledError handledError) {
        int createRet = this.createBasicCredentialForConsumer(consumerName, userName, password, idStr,
                handledError);
        if (createRet == 201) {
            return 0;
        }
        if (createRet != 409) {
            handledError.setErrorMsg("Could not create basic credential for consumer of " + consumerName
                    + ": " + handledError.getErrorMsg());
            return -1;
        }
        if (this.getBasicCredentialForConsumer(consumerName, userName, idStr, handledError) != 200) {
            handledError.setErrorMsg("Could not get basic credential for consumer of " + consumerName
                    + ": " + handledError.getErrorMsg());
            return -1;
        }
        if (this.updateCredentialConsumer(consumerName, idStr.toString(), userName, password,
                handledError) != 200) {
            handledError.setErrorMsg("Could not update basic credential for consumer of " + consumerName
                    + ": " + handledError.getErrorMsg());
            return -1;
        }
        return 0;
    }

    public int getBasicCredentialForConsumer(String consumerName, String userName, StringBuilder idStr
            , HandledError handledError) {
        int retInt = -1;
        if ((consumerName == null) || (userName == null)) {
            handledError.setErrorMsg("Null parameters");
            return retInt;
        }
        if ((consumerName.isEmpty()) || (userName.isEmpty())) {
            handledError.setErrorMsg("Null parameters");
            return retInt;
        }
        String resultAsJsonStr = "";
        try {
            String kongServiceUrl = kongManagementUrl + "/consumers/" + consumerName + "/basic-auth";

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<String>(headers);
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
                if (authObject.get("username").getAsString().equals(userName)) {
                    foundAuth = true;
                    idStr.setLength(0);
                    idStr.append(authObject.get("id").getAsString());
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

    public int createBasicCredentialForConsumer(String consumerName, String userName, String password,
                                                StringBuilder idStr, HandledError handledError) {
        int retInt = 201;
        if ((consumerName == null) || (userName == null) || (password == null)) {
            handledError.setErrorMsg("Null parameters");
            return -1;
        }
        if ((consumerName.isEmpty()) || (userName.isEmpty())
                || (password.isEmpty())) {
            handledError.setErrorMsg("Null parameters");
            return -1;
        }

        String resultAsJsonStr = "";
        try {
            String kongServiceUrl = kongManagementUrl + "/consumers/" + consumerName + "/basic-auth";
            RestClient restTemplate = new RestClient();
            String body = String.format("{" +
                    "\"username\":\"%s\"," +
                    "\"password\": \"%s\"" +
                    "}", userName, password);

            resultAsJsonStr = restTemplate.post(kongServiceUrl, body);
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            idStr.setLength(0);
            idStr.append(convertedObject.get("id").getAsString());
        } catch (HttpClientErrorException httpClientErrorException) {
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -1;
        }
        return retInt;
    }

    public KongOAuthClient createOAuthClientForConsumer(String consumerName, String clientName,
                                                        String clientRedirectUri, HandledError handledError) {
        if ((consumerName == null) || (clientName == null) || (clientRedirectUri == null)) {
            handledError.setErrorMsg("Null parameters");
            return null;
        }
        if ((consumerName.isEmpty()) || (clientName.isEmpty()) || (clientRedirectUri.isEmpty())) {
            handledError.setErrorMsg("Null parameters");
            return null;
        }
        KongOAuthClient kongOAuthClient = null;
        String resultAsJsonStr = null;
        try {
            String kongServiceUrl = kongManagementUrl + "/consumers/" + consumerName + "/oauth2";
            RestClient restTemplate = new RestClient();
            String body = String.format("{" +
                    "\"name\":\"%s\",\n\"redirect_uris\":[\"%s\"]" +
                    "}", clientName, clientRedirectUri);

            resultAsJsonStr = restTemplate.post(kongServiceUrl, body);
            kongOAuthClient = new KongOAuthClient();
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            String idString = convertedObject.get("id").getAsString();
            kongOAuthClient.setKongId(idString);
            kongOAuthClient.setClientId(convertedObject.get("client_id").getAsString());
            kongOAuthClient.setClientSecret(convertedObject.get("client_secret").getAsString());
            kongOAuthClient.setClientRedirectUri(clientRedirectUri);
            kongOAuthClient.setClientName(clientName);

        } catch (HttpClientErrorException httpClientErrorException) {
            handledError.setErrorMsg(httpClientErrorException.getMessage());
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
        }
        return kongOAuthClient;
    }

    public int createOrUpdateApiKeyForConsumer(String consumerName, String apiKey, StringBuilder keyId,
                                               HandledError handledError) {
        int createRet = this.createApiKeyForConsumer(consumerName, apiKey, keyId, handledError);
        if (createRet == 201) { //Successful
            return 0;
        }
        if (createRet != 409) {
            handledError.setErrorMsg("Could not create API key for consumer of " + consumerName
                    + ": " + handledError.getErrorMsg());
            return -1;
        }
        if (this.getApiKeyForConsumer(consumerName, apiKey, keyId, handledError) != 200) {
            handledError.setErrorMsg("Could not get API key for consumer of " + consumerName
                    + ": " + handledError.getErrorMsg());
            return -1;
        }
        return 0;
    }

    public int createApiKeyForConsumer(String consumerName, String apiKey, StringBuilder keyId,
                                       HandledError handledError) {
        int retInt = 201;
        if ((consumerName == null) || (apiKey == null)) {
            handledError.setErrorMsg("Null parameters");
            return -1;
        }
        if ((consumerName.isEmpty()) || (apiKey.isEmpty())) {
            handledError.setErrorMsg("Null parameters");
            return -1;
        }

        String resultAsJsonStr = "";
        try {
            String kongServiceUrl = kongManagementUrl + "/consumers/" + consumerName + "/key-auth";
            RestClient restTemplate = new RestClient();
            String body = String.format("{" +
                    "\"key\":\"%s\"" +
                    "}", apiKey);

            resultAsJsonStr = restTemplate.post(kongServiceUrl, body);
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            keyId.setLength(0);
            keyId.append(convertedObject.get("id").getAsString());
        } catch (HttpClientErrorException httpClientErrorException) {
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -2;
        }
        return retInt;
    }

    public int getApiKeyForConsumer(String consumerName, String apiKey, StringBuilder keyId,
                                    HandledError handledError) {
        int retInt = -1;
        if ((consumerName == null) || (apiKey == null)) {
            handledError.setErrorMsg("Null parameters");
            return retInt;
        }
        if ((consumerName.isEmpty()) || (apiKey.isEmpty())) {
            handledError.setErrorMsg("Null parameters");
            return retInt;
        }
        String resultAsJsonStr = "";
        try {
            String kongServiceUrl = kongManagementUrl + "/consumers/" + consumerName + "/key-auth";

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<String>(headers);
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
                if (authObject.get("key").getAsString().equals(apiKey)) {
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

    public int updateCredentialConsumer(String consumerName, String credentialId,
                                        String username, String password, HandledError handledError) {
        int retInt = 200;
        if ((consumerName == null) || (credentialId == null) || (username == null)
                || (password == null)) {
            handledError.setErrorMsg("Null parameters");
            return -1;
        }
        if ((consumerName.isEmpty()) || (credentialId.isEmpty()) || (username.isEmpty())
                || (password.isEmpty())) {
            handledError.setErrorMsg("Empty parameters");
            return -1;
        }

        try {
            String kongServiceUrl = kongManagementUrl + "/consumers/" + consumerName + "/basic-auth/" + credentialId;
            RestClient restTemplate = new RestClient();
            String body = String.format("{" +
                    "\"username\":\"%s\"," +
                    "\"password\": \"%s\"" +
                    "}", username, password);

            String resultAsJsonStr = restTemplate.put(kongServiceUrl, body);
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -2;
        }
        return retInt;
    }

    public int updateApiKeyConsumer(String consumerName, String keyId, String apiKey, HandledError handledError) {
        int retInt = 200;
        if ((consumerName == null) || (keyId == null) || (apiKey == null)) {
            return -1;
        }
        if ((consumerName.isEmpty()) || (keyId.isEmpty()) || (apiKey.isEmpty())) {
            return -1;
        }

        try {
            String kongServiceUrl = kongManagementUrl + "/consumers/" + consumerName + "/key-auth/" + keyId;
            RestClient restTemplate = new RestClient();
            String body = String.format("{" +
                    "\"key\":\"%s\"" +
                    "}", apiKey);

            String resultAsJsonStr = restTemplate.put(kongServiceUrl, body);
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -2;
        }
        return retInt;
    }

    public int createJwtClientForConsumer(String consumerName, ConsumerJwtCredential jwt, StringBuilder idPlugin, HandledError handledError) {
        if ((consumerName.isEmpty()) || (consumerName == null) || (jwt == null)) {
            handledError.setErrorMsg("Null parameters");
            return -1;
        }
        ConsumerJwtCredential kongJwtClient = null;
        try {
            KongConsumerCredential credential = new KongConsumerCredential(kongManagementUrl);
            return credential.createOrUpdateJwtForConsumer(consumerName, jwt, idPlugin, handledError);
        } catch (HttpClientErrorException httpClientErrorException) {
            handledError.setErrorMsg(httpClientErrorException.getMessage());
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
        }
        return -1;
    }

    public int createOrUpdateUpStream(String upstreamName, KongUpstreamDetails kongUpstreamDetails,
                                      HandledError handledError) {
        int intRet = 0;
        int intUpstream = this.createUpStream(upstreamName, kongUpstreamDetails, handledError);
        if (intUpstream != 201) {
            if (intUpstream != 409) {
                handledError.setErrorMsg("Could not create upstream on the API Gateway: " +
                        handledError.getErrorMsg());
                intRet = -1;
            }
        }
        return intRet;
    }

    public int createUpStream(String upstreamName, KongUpstreamDetails kongUpstreamDetails,
                              HandledError handledError) {
        int retInt = -1;
        if (upstreamName == null) {
            handledError.setErrorMsg("Upstream name is Null");
            return retInt;
        }
        if (upstreamName.isEmpty()) {
            handledError.setErrorMsg("Upstream name is Empty");
            return retInt;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("name", upstreamName);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
            String kongServiceUrl = kongManagementUrl + "/upstreams";

            ResponseEntity<String> result = restTemplate.postForEntity(kongServiceUrl, request, String.class);
            retInt = result.getStatusCodeValue();

            JsonObject convertedObject = new Gson().fromJson(result.getBody(), JsonObject.class);
            String idString = convertedObject.get("id").getAsString();
            kongUpstreamDetails.setId(idString);
            kongUpstreamDetails.setName(convertedObject.get("name").getAsString());
            //kongUpstreamDetails.setAlgorithm(convertedObject.get("algorithm(").getAsString());
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

    public int updateUpstream(KongUpstreamDetails kongUpstreamDetails, HandledError handledError) {
        int retInt = 200;
        return retInt;
    }

    public KongTargetDetails getUpstreamTargetByName(String upstreamName, String targetName,
                                                     HandledError handledError) {
        if (targetName == null) {
            handledError.setErrorMsg("Target name is NULL");
            return null;
        }
        KongTargetDetails[] kongTargetDetailsList = this.getUpstreamTargets(upstreamName, handledError);
        KongTargetDetails kongTargetDetails = null;
        if (kongTargetDetailsList == null) {
            return null;
        }
        for (int i = 0; i < kongTargetDetailsList.length; i++) {
            if (kongTargetDetailsList[i].getName().equals(targetName)) {
                kongTargetDetails = kongTargetDetailsList[i];
                break;
            }
        }
        return kongTargetDetails;
    }

    public KongTargetDetails[] getUpstreamTargets(String upstreamName, HandledError handledError) {
        if (upstreamName == null) {
            return null;
        }
        if (upstreamName.isEmpty()) {
            return null;
        }
        KongTargetDetails[] kongTargetDetails = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response =
                    restTemplate.getForEntity(
                            kongManagementUrl + "/upstreams/" + upstreamName + "/targets",
                            String.class);
            JsonObject convertedObject = new Gson().fromJson(response.getBody(), JsonObject.class);
            JsonArray jsonArray = convertedObject.get("data").getAsJsonArray();
            if (jsonArray.size() > 0) {
                kongTargetDetails = new KongTargetDetails[jsonArray.size()];
                for (int i = 0; i < jsonArray.size(); i++) {
                    String idStr = jsonArray.get(i).getAsJsonObject().get("id").getAsString();
                    String nameStr = jsonArray.get(i).getAsJsonObject().get("target").getAsString();
                    Long weight = jsonArray.get(i).getAsJsonObject().get("weight").getAsLong();
                    kongTargetDetails[i] = new KongTargetDetails();
                    kongTargetDetails[i].setId(idStr);
                    kongTargetDetails[i].setName(nameStr);
                    kongTargetDetails[i].setWeight(weight);
                }
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            kongTargetDetails = null;
        } catch (Exception e) {
            kongTargetDetails = null;
        }
        return kongTargetDetails;
    }

    public int createOrUpdateUpstreamTarget(String upstreamName, String hostName, Long hostPort,
                                            Long weight, KongTargetDetails kongTargetDetails, HandledError handledError) {
        int intRet = 0;
        int intTarget = this.createUpstreamTarget(upstreamName, hostName, hostPort, weight, kongTargetDetails,
                handledError);
        if (intTarget != 201) {
            if (intTarget == 409) {
                KongTargetDetails existingTarget = this.getUpstreamTargetByName(upstreamName,
                        hostName + ":" + hostPort.toString(), handledError);
                if (existingTarget == null) {
                    handledError.setErrorMsg("Could not create target: " + handledError.getErrorMsg());
                    intRet = -1;
                } else {
                    if (this.updateUpstreamTarget(upstreamName, existingTarget.getId(),
                            hostName, hostPort, weight, handledError) != 200) {
                        handledError.setErrorMsg("Could not update existing target: " + handledError.getErrorMsg());
                        intRet = -1;
                    }
                }
            } else {
                handledError.setErrorMsg("Could not create target: " + handledError.getErrorMsg());
                intRet = -1;
            }
        }
        return intRet;
    }

    public int createUpstreamTarget(String upstreamName, String hostName, Long hostPort,
                                    Long weight, KongTargetDetails kongTargetDetails, HandledError handledError) {
        int retInt = 201;
        if ((hostName == null) || (upstreamName == null)) {
            handledError.setErrorMsg("Backend host Null");
            return retInt;
        }
        if ((hostName.isEmpty()) || (upstreamName.isEmpty())) {
            handledError.setErrorMsg("Backend host Empty");
            return retInt;
        }
        if ((hostPort <= 0) || (weight <= 0)) {
            handledError.setErrorMsg("Backend host port or weight less than Zero");
            return retInt;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            String postStr = "{\"target\":\"" + hostName + ":" +
                    hostPort.toString() + "\",\n\"weight\":" + weight.toString() + "}";

            HttpEntity<String> request =
                    new HttpEntity<String>(postStr, headers);
            String kongServiceUrl = kongManagementUrl + "/upstreams/" + upstreamName + "/targets";
            String resultAsJsonStr =
                    restTemplate.postForObject(kongServiceUrl, request, String.class);
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            kongTargetDetails.setId(convertedObject.get("id").getAsString());
            kongTargetDetails.setName(convertedObject.get("target").getAsString());
            kongTargetDetails.setWeight(convertedObject.get("weight").getAsLong());
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

    public int updateUpstreamTarget(String upstreamName, String targetId, String hostName, Long hostPort,
                                    Long weight, HandledError handledError) {
        int retInt = 200;

        if ((hostName == null) || (upstreamName == null) || (targetId == null)) {
            handledError.setErrorMsg("Backend host or Target id Null");
            return retInt;
        }
        if ((hostName.isEmpty()) || (upstreamName.isEmpty())
                || (targetId.isEmpty())) {
            handledError.setErrorMsg("Backend host or Target id Empty");
            return retInt;
        }
        if ((hostPort <= 0) || (weight <= 0)) {
            handledError.setErrorMsg("Backend host port or weight less than Zero");
            return retInt;
        }

        KongTargetDetails kongTargetDetails = new KongTargetDetails();
        kongTargetDetails.setName(hostName);
        kongTargetDetails.setWeight(weight);
        kongTargetDetails.setId(targetId);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<KongTargetDetails> entity = new HttpEntity<KongTargetDetails>(kongTargetDetails, headers);
        RestTemplate restTemplate = new RestTemplate();

        String putUrl = kongManagementUrl + "/upstreams/" + upstreamName + "/target/" +
                targetId;

        try {
            restTemplate.exchange(putUrl, HttpMethod.PUT, entity, String.class).getBody();
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

    public int deleteUpstreamDelete(String upstreamName, String targetId, HandledError handledError) {
        int retInt = -1;
        if ((upstreamName == null) || (targetId == null)) {
            handledError.setErrorMsg("Upstream or Target id Null");
            return retInt;
        }
        if ((upstreamName.isEmpty()) || (targetId.isEmpty())) {
            handledError.setErrorMsg("Upstream or Target id Empty");
            return retInt;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            String deleteUrl = kongManagementUrl + "/upstreams/" + upstreamName + "/target/" +
                    targetId;

            HttpHeaders headers = new HttpHeaders();

            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<KongServiceDetails> entity = new HttpEntity<KongServiceDetails>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);
            retInt = response.getStatusCodeValue();
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            handledError.setErrorMsg(httpClientErrorException.getMessage());
        } catch (Exception e) {
            retInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return retInt; //204 succesfull
    }

    public int createOrUpdateUpstreamHeaderPlugin(String serviceName, String serviceSecurityMethod,
                                                  String headerName, String headerValue, String requestTransformer, StringBuilder pluginId,
                                                  HandledError handledError) {
        int createRet = this.createUpstreamHeaderPlugin(serviceName, serviceSecurityMethod,
                headerName, headerValue, pluginId, handledError, requestTransformer);
        if (createRet == 201) { //Successful
            return 0;
        }
        if (createRet != 409) {
            handledError.setErrorMsg("Could not create Update stream header: "
                    + handledError.getErrorMsg());
            return -1;
        }
        KongServicePlugin kongServicePlugin = this.getServicePluginByType(serviceName, "request-transformer", handledError);
        if (kongServicePlugin == null) {
            handledError.setErrorMsg("Could not get Update stream header: "
                    + handledError.getErrorMsg());
            return -1;
        }
        int updateRet = this.updateUpstreamHeader(serviceName, serviceSecurityMethod, headerName, headerValue,
                kongServicePlugin.getKongId(),
                handledError, requestTransformer);
        if (updateRet != 200) {
            handledError.setErrorMsg("Could not update Update stream header: "
                    + handledError.getErrorMsg());
            return -1;
        }
        return 0;
    }

    private ArrayList<String> addItemToList(ArrayList<String> res, ArrayList<String> lst) {
        if (lst == null) {
            return res;
        }
        if (lst.isEmpty()) {
            return res;
        }
        for (String i : lst) {
            if (i != null) {
                res.add(i);
            }
        }
        return res;
    }

    private void getConfigRequestTranform(String _requestTransformer, ArrayList<String> addBody, ArrayList<String> removeBody, ArrayList<String> renameBody, ArrayList<String> replaceBody, ArrayList<String> appendBody,
                                          ArrayList<String> addQuerystring, ArrayList<String> removeQuerystring, ArrayList<String> renameQuerystring, ArrayList<String> replaceQuerystring, ArrayList<String> appendQuerystring, HandledError handledError) {

        if (_requestTransformer != null) {
            if (!_requestTransformer.isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    RequestTransformerTransformationsPlugin plugin = objectMapper.readValue(_requestTransformer, RequestTransformerTransformationsPlugin.class);
                    RequestTransformerTransformationsPluginConfig configFromDB = plugin.getConfig();
                    addBody = addItemToList(addBody, configFromDB.getAdd().getBody());
                    removeBody = addItemToList(removeBody, configFromDB.getRemove().getBody());
                    renameBody = addItemToList(renameBody, configFromDB.getRename().getBody());
                    replaceBody = addItemToList(replaceBody, configFromDB.getReplace().getBody());
                    appendBody = addItemToList(appendBody, configFromDB.getAppend().getBody());

                    addQuerystring = addItemToList(addQuerystring, configFromDB.getAdd().getQuerystring());
                    removeQuerystring = addItemToList(removeQuerystring, configFromDB.getRemove().getQuerystring());
                    renameQuerystring = addItemToList(renameQuerystring, configFromDB.getRename().getQuerystring());
                    replaceQuerystring = addItemToList(replaceQuerystring, configFromDB.getReplace().getQuerystring());
                    appendQuerystring = addItemToList(appendQuerystring, configFromDB.getAppend().getQuerystring());
                } catch (Exception e) {
                    handledError.setErrorMsg(e.getMessage());
                }
            }

        }
    }

    public int createUpstreamHeaderPlugin(String serviceName, String serviceSecurityMethod,
                                          String headerName, String headerValue, StringBuilder pluginId,
                                          HandledError handledError, String _requestTransformer) {
        int retInt = 201;
        if ((serviceName == null) || (pluginId == null)) {
            handledError.setErrorMsg("Parameter is Null when setting request transformer");
            return -1;
        }
        if (serviceName.isEmpty()) {
            handledError.setErrorMsg("Parameter is Empty when setting request transformer");
            return -1;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            RequestTransformerTransformationsPluginConfig config = new RequestTransformerTransformationsPluginConfig();
            ArrayList<String> addBody = new ArrayList<String>(), addQuerystring = new ArrayList<String>();
            ArrayList<String> removeBody = new ArrayList<String>(), removeQuerystring = new ArrayList<String>();
            ArrayList<String> renameBody = new ArrayList<String>(), renameQuerystring = new ArrayList<String>();
            ArrayList<String> replaceBody = new ArrayList<String>(), replaceQuerystring = new ArrayList<String>();
            ArrayList<String> appendBody = new ArrayList<String>(), appendQuerystring = new ArrayList<String>();
            RequestTransformerTransformationsPluginConfig configFromDB = new RequestTransformerTransformationsPluginConfig();
            if (_requestTransformer != null) {
                if (!_requestTransformer.isEmpty()) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        RequestTransformerTransformationsPlugin plugin = objectMapper.readValue(_requestTransformer, RequestTransformerTransformationsPlugin.class);
                        configFromDB = plugin.getConfig();
//                        config.setAppend(configFromDB.getAppend());
//                        config.setRename(configFromDB.getRename());
//                        config.setReplace(configFromDB.getReplace());
                        config = configFromDB;

                    } catch (Exception e) {
                        handledError.setErrorMsg("Error in create upstream header. " + e.getMessage());
                    }
                }
            }
            getConfigRequestTranform(_requestTransformer, addBody, removeBody, renameBody, replaceBody, appendBody,
                    addQuerystring, removeQuerystring, renameQuerystring, replaceQuerystring, appendQuerystring, handledError);

            ArrayList<String> addContent;
            if (!StringUtils.hasText(headerName)) {
                addContent = null;
            } else {
                addContent = new ArrayList<>(Arrays.asList(headerName + ":" + headerValue));
            }


            RequestAdd add = new RequestAdd(addBody, addContent, configFromDB.getAdd() == null ? null : configFromDB.getAdd().getQuerystring());
            config.setAdd(add);

            config.setAppend(new RequestAppend(appendBody, null, appendQuerystring));
            config.setRename(new RequestRename(renameBody, null, renameQuerystring));
            config.setRemove(new RequestRemove(removeBody, config.getRemove() == null ? null : (config.getRemove().getHeaders() == null ? null : config.getRemove().getHeaders()), removeQuerystring));
            config.setReplace(new RequestReplace(replaceBody, config.getReplace() == null ? null : (config.getReplace().getHeaders() == null ? null : config.getReplace().getHeaders()), replaceQuerystring, null));

            RequestRemove remove = new RequestRemove(removeBody, null, configFromDB.getRemove() == null ? null : configFromDB.getRemove().getQuerystring());


            String postStr = "";
            if (headerName != null && headerValue != null && serviceSecurityMethod == null) {
                if (!headerName.isBlank() && !headerValue.isBlank() && !serviceSecurityMethod.isBlank()) {
                    if (serviceSecurityMethod.equals("Basic")
                            || serviceSecurityMethod.equals("OAuth")
                            || serviceSecurityMethod.equals("JWT")) {

                        if (headerName.toLowerCase().equals("authorization")) {
                            ArrayList<String> removeContent = new ArrayList<>(Arrays.asList("Authorization", "api-key"));
                            remove.setHeaders(removeContent);
                            config.setRemove(remove);
                        } else {
                            ArrayList<String> removeContent = new ArrayList<>(Arrays.asList("Authorization", "api-key"));
                            remove.setHeaders(removeContent);
                            config.setRemove(remove);
                        }
                    } else {
//                RequestRemove remove = new RequestRemove(null, removeBody, null);
                        config.setRemove(remove);
                    }
                }
            }


            RequestTransformerTransformationsPlugin requestTransformer = new RequestTransformerTransformationsPlugin(config);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            postStr = objectMapper.writeValueAsString(requestTransformer);

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
            handledError.setErrorMsg("HttpClientErrorException in create upstream header. " + httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        } catch (Exception e) {
            handledError.setErrorMsg("Exception in create upstream header. " + e.getMessage());
            retInt = -1;
        }
        return retInt;
    }

    public int updateUpstreamHeader(String serviceName, String serviceSecurityMethod, String headerName,
                                    String headerValue, String pluginId, HandledError handledError, String _requestTransformer) {
        RequestTransformerTransformationsPluginConfig config = new RequestTransformerTransformationsPluginConfig();
        ArrayList<String> addBody = new ArrayList<String>(), addQuerystring = new ArrayList<String>();
        ArrayList<String> removeBody = new ArrayList<String>(), removeQuerystring = new ArrayList<String>();
        ArrayList<String> renameBody = new ArrayList<String>(), renameQuerystring = new ArrayList<String>();
        ArrayList<String> replaceBody = new ArrayList<String>(), replaceQuerystring = new ArrayList<String>();
        ArrayList<String> appendBody = new ArrayList<String>(), appendQuerystring = new ArrayList<String>();

        getConfigRequestTranform(_requestTransformer, addBody, removeBody, renameBody, replaceBody, appendBody,
                addQuerystring, removeQuerystring, renameQuerystring, replaceQuerystring, appendQuerystring, handledError);

        int retInt = 200;

        if ((serviceName == null) || (pluginId == null)) {
            handledError.setErrorMsg("Parameter is Null");
            return -1;
        }
        if (serviceName.isEmpty() || (pluginId.isEmpty())) {
            handledError.setErrorMsg("Parameter is Empty");
            return -1;
        }

        ArrayList<String> addContent = null;

        if (headerName != null && headerValue != null) {
            if (!headerName.isBlank() && !headerValue.isBlank()) {
                addContent = new ArrayList<>(Arrays.asList(headerName + ":" + headerValue));
            }
        }
        RequestAdd add = new RequestAdd(addBody, addContent, addQuerystring);
        config.setAdd(add);

        config.setAppend(new RequestAppend(appendBody, null, appendQuerystring));
        config.setRename(new RequestRename(renameBody, null, renameQuerystring));
        config.setRemove(new RequestRemove(removeBody, config.getRemove() == null ? null : (config.getRemove().getHeaders() == null ? null : config.getRemove().getHeaders()), removeQuerystring));
        config.setReplace(new RequestReplace(replaceBody, config.getReplace() == null ? null : (config.getReplace().getHeaders() == null ? null : config.getReplace().getHeaders()), replaceQuerystring, null));

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        String putStr = "";
        if (headerName != null && headerValue != null && serviceSecurityMethod == null) {
            if (!headerName.isBlank() && !headerValue.isBlank() && !serviceSecurityMethod.isBlank()) {
                if (serviceSecurityMethod.equals("Basic")
                        || serviceSecurityMethod.equals("OAuth")
                        || serviceSecurityMethod.equals("JWT")) {
                    if (headerName.toLowerCase().equals("authorization")) {
                        ArrayList<String> removeContent = new ArrayList<>(Arrays.asList("Authorization", "api-key"));
                        removeContent.addAll(removeBody);
                        RequestRemove remove = new RequestRemove(null, removeContent, null);
                        config.setRemove(remove);
                    } else {
                        ArrayList<String> removeContent = new ArrayList<>(Arrays.asList("Authorization", "api-key"));
                        removeContent.addAll(removeBody);
                        RequestRemove remove = new RequestRemove(null, removeContent, null);
                        config.setRemove(remove);
                    }
                } else {
                    RequestRemove remove = new RequestRemove(null, removeBody, null);
                    config.setRemove(remove);
                }
            }
        }

        RequestTransformerTransformationsPlugin requestTransformer = new RequestTransformerTransformationsPlugin(config);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        RestTemplate restTemplate = new RestTemplate();
        try {
            String kongServiceUrl = kongManagementUrl + "/services/" + serviceName
                    + "/plugins/" + pluginId;
            putStr = objectMapper.writeValueAsString(requestTransformer);
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

    public int createOrUpdateResponseTransformerPlugin(String serviceName, String responseTransformer, StringBuilder pluginId,
                                                       HandledError handledError) {
        KongTransformationPlugin kongTransformation = new KongTransformationPlugin(kongManagementUrl);
        int createRet = kongTransformation.createResponseTransformerPlugin(serviceName, pluginId, handledError, responseTransformer);
        if (createRet == 201) { //Successful
            return 0;
        }
        if (createRet != 409) {
            handledError.setErrorMsg("Could not create Update stream header: "
                    + handledError.getErrorMsg());
            return -1;
        }
        KongServicePlugin kongServicePlugin = this.getServicePluginByType(serviceName, "request-transformer", handledError);
        if (kongServicePlugin == null) {
            handledError.setErrorMsg("Could not get Update stream header: "
                    + handledError.getErrorMsg());
            return -1;
        }
        int updateRet = kongTransformation.updateResponseTransformerPlugin(serviceName,
                kongServicePlugin.getKongId(),
                handledError, responseTransformer);
        if (updateRet != 200) {
            handledError.setErrorMsg("Could not update Update stream header: "
                    + handledError.getErrorMsg());
            return -1;
        }
        return 0;
    }

    public int createOrUpdateCorsPlugin(String serviceName, String value, StringBuilder pluginId,
                                        HandledError handledError) {
        Cors action = new Cors(kongManagementUrl);
        return action.createOrUpdate(serviceName, value, pluginId, handledError);
    }

    public String getRawServicePlugins(String serviceName, HandledError handledError) {
        if (!StringUtils.hasText(serviceName)) {
            handledError.setErrorMsg("Service name is Null or Empty");
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response =
                    restTemplate.getForEntity(
                            kongManagementUrl + "/services/" + serviceName + "/plugins",
                            String.class);
            return response.getBody();
        } catch (HttpClientErrorException httpClientErrorException) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public int createOrUpdateIpRestriction(String serviceName, IpRestrictionSecurityPlugin plugin, StringBuilder pluginId, HandledError handledError) {
        if (!StringUtils.hasText(serviceName) || plugin == null) {
            handledError.setErrorMsg("Service name or plugin is Null or Empty");
            return -1;
        }
        KongSecurityPlugin kongSecurityPlugin = new KongSecurityPlugin(kongManagementUrl);

        int createRet = kongSecurityPlugin.createIpRestrictionPlugin(serviceName, pluginId, plugin, handledError);
        if (createRet == 201) { //Successful
            return 0;
        }

        if (createRet != 409) {
            handledError.setErrorMsg("Could not create Update stream header: "
                    + handledError.getErrorMsg());
            return -1;
        }
        KongServicePlugin kongServicePlugin = this.getServicePluginByType(serviceName, "ip-restriction", handledError);
        if (kongServicePlugin == null) {
            handledError.setErrorMsg("Could not get Ip Restriction: "
                    + handledError.getErrorMsg());
            return -1;
        }
        int updateRet = kongSecurityPlugin.updateIpRestrictionPlugin(serviceName, plugin,
                kongServicePlugin.getKongId(), handledError);
        if (updateRet != 200) {
            handledError.setErrorMsg("Could not update Ip Restriction: "
                    + handledError.getErrorMsg());
            return -1;
        }
        return 0;
    }

    public int deletePluginWithType(String serviceName, String pluginType, HandledError handledError) {
//        "ip-restriction"
        {
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

    }

    public int createOrUpdateValidationSchemaPlugin(String serviceName, RestApi restApi, HandledError handledError) {
        KongServicePlugin kongServicePlugin = this.getServicePluginByType(serviceName, "schema-validator", handledError);
        if (kongServicePlugin == null) {
            int createRet = this.createValidationSchemaPlugin(serviceName, restApi, handledError);
            if (createRet == 201) { //Successful
                return 0;
            }
            if (createRet != 409) {
                handledError.setErrorMsg("Could not create validation schema: "
                        + handledError.getErrorMsg());
                return -1;
            }
        } else {
            int updateRet = this.updateValidationSchema(serviceName, restApi, kongServicePlugin.getKongId(), handledError);
            if (updateRet != 200) {
                handledError.setErrorMsg("Could not update validation schema: "
                        + handledError.getErrorMsg());
                return -1;
            }
        }

        return 0;
    }

    public int createValidationSchemaPlugin(String serviceName, RestApi restApi, HandledError handledError) {
        int retInt = 201;
        if ((serviceName == null)) {
            handledError.setErrorMsg("Parameter is Null");
            return -1;
        }
        if (serviceName.isEmpty()) {
            handledError.setErrorMsg("Parameter is Empty");
            return -1;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            JsonObject config = new JsonObject();

            if (restApi.getBody_schema() != null && !restApi.getBody_schema().isBlank()) {
                config.add("body_schema", new Gson().fromJson(restApi.getBody_schema(), JsonElement.class));
            }

            if (restApi.getParameter_schema() != null && !restApi.getParameter_schema().isBlank()) {
                config.add("parameter_schema", new Gson().fromJson(restApi.getParameter_schema(), JsonElement.class));
            }

            if (restApi.getAllow_content_type() != null && !restApi.getAllow_content_type().isBlank()) {
                config.add("allowed_content_types", new Gson().fromJson(restApi.getAllow_content_type(), JsonElement.class));
            }

            if (restApi.getVerbose_response() != null) {
                config.addProperty("verbose_response", restApi.getVerbose_response());
            }

            String postStr = "{\n" +
                    "    \"name\": \"schema-validator\",\n" +
                    "    \"config\":\n" + config +
                    "}";

            System.out.println("Post string: " + postStr);

            HttpEntity<String> request =
                    new HttpEntity<String>(postStr, headers);
            String kongServiceUrl = kongManagementUrl + "/services/" + serviceName + "/plugins";

            String resultAsJsonStr =
                    restTemplate.postForObject(kongServiceUrl, request, String.class);
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
        }
        catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        }
        catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -1;
        }
        return retInt;
    }

    public int updateValidationSchema(String serviceName, RestApi restApi, String pluginId, HandledError handledError) {
        int retInt = 200;

        if (serviceName == null) {
            handledError.setErrorMsg("Parameter is Null");
            return -1;
        }
        if (serviceName.isEmpty()) {
            handledError.setErrorMsg("Parameter is Empty");
            return -1;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        JsonObject config = new JsonObject();

        if (restApi.getBody_schema() != null && !restApi.getBody_schema().isBlank()) {
            config.add("body_schema", new Gson().fromJson(restApi.getBody_schema(), JsonElement.class));
        }

        if (restApi.getParameter_schema() != null && !restApi.getParameter_schema().isBlank()) {
            config.add("parameter_schema", new Gson().fromJson(restApi.getParameter_schema(), JsonElement.class));
        }

        if (restApi.getAllow_content_type() != null && !restApi.getAllow_content_type().isBlank()) {
            config.add("allowed_content_types", new Gson().fromJson(restApi.getAllow_content_type(), JsonElement.class));
        }

        if (restApi.getVerbose_response() != null) {
            config.addProperty("verbose_response", restApi.getVerbose_response());
        }

        String putStr = "{\n" +
                "    \"name\": \"schema-validator\",\n" +
                "    \"config\": " + config + "\n" +
                "}";

        System.out.println("Put string: " + putStr);
        HttpEntity<String> entity = new HttpEntity<String>(putStr, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            String kongServiceUrl = kongManagementUrl + "/services/" + serviceName
                    + "/plugins/" + pluginId;
            String resultAsJsonStr = restTemplate.exchange(kongServiceUrl, HttpMethod.PUT, entity,
                    String.class).getBody();
        }
        catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        }
        catch (Exception e) {
            retInt = -1;
            handledError.setErrorMsg(e.getMessage());
        }
        return retInt;
    }
}