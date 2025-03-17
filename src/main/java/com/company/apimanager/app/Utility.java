package com.company.apimanager.app;

import com.company.apimanager.entity.*;
import com.company.apimanager.screen.brandlogin.ApiRegister;
import com.company.apimanager.screen.publishedproduct.HandledError;
import com.google.gson.*;
import io.jmix.core.DataManager;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.ui.component.ValuesPicker;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static org.eclipse.persistence.eis.EISException.TIMEOUT;

@Component
public class Utility {
    @Autowired
    private DataManager dataManager;

    @Value("${bss.authenticate.url}")
    protected String bssAuthUrl;

    @Value("${bss.authenticate.token}")
    protected String bssAuthToken;

    @Value("${application.url}")
    protected String appUrl;

    @Value("${basic.auth}")
    protected String basicAuth;

    @Value("${jmix.admin.account}")
    protected String jmixAdminAccount;

    @Value("${jmix.admin.password}")
    protected String jmixAdminPassword;

    @Value("${jmix.client.base_auth}")
    protected String jmixClientBaseAuth;
    @Value("${jmix.base_url}")
    protected String jmixBaseUrl;

    private static final File TEMP_DIRECTORY = new File("/usr/local/tomcat");
    private final Logger log = LoggerFactory.getLogger(Utility.class);

    public static boolean CheckNullAndEmpty(String str) {
        if (str == null) {
            return true;
        }
        if (str.isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean validateUrl(String str) {
        boolean rs = true;
        if (str == null) {
            return false;
        }
        if (str.isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
                , Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        rs = matcher.find();
        return rs;
    }

//    public List<User> GetProviderUser() {
//        //List<User> u = dataManager.load(User.class).all().list();
//        //List<User> u = dataManager.load(User.class).query("e.username = ?1", "admin").list();
//
//        List<User> u = dataManager.load(User.class).query("from User u, sec_RoleAssignmentEntity a where u.username = a.username and a.roleCode = ?1", "api_provider_owner").list();
//        return u;
//    }

    public int CreateRoleAssignment(String user_name, String role_code, String role_type) {
        int retInt = 0;
        boolean foundEntity = false;

        try {
            //Check if role already exist
            List<RoleAssignmentEntity> roleAssignmentEntities
                    = dataManager.load(RoleAssignmentEntity.class)
                    .query("select e from sec_RoleAssignmentEntity e where e.username = :username and e.roleCode = :role_code")
                    .parameter("username", user_name).parameter("role_code", role_code).list();

            if (roleAssignmentEntities != null) {
                if (roleAssignmentEntities.size() > 0) {
                    foundEntity = true;
                }
            }
        } catch (Exception e) {
            log.error("Error when find exist role: " + e.getMessage());
            retInt = -1;
        }
        if (retInt == -1) {
            return retInt;
        }

        if (foundEntity) { //Role exists
            return 0;
        }

        try {
            RoleAssignmentEntity roleAssignmentEntity = dataManager.create(RoleAssignmentEntity.class);
            roleAssignmentEntity.setUsername(user_name);
            roleAssignmentEntity.setRoleCode(role_code);
            roleAssignmentEntity.setRoleType(role_type);

            dataManager.save(roleAssignmentEntity);
        } catch (Exception e) {
            log.error("Error when create role: " + e.getMessage());
            retInt = -2;
        }
        return retInt;
    }

    public static int findSpecialChar(String s) {
        int intRet = 0;
        if (s == null) {
            return -1;
        }
        if ((s.indexOf(" ") >= 0) || (s.indexOf("*") >= 0) || (s.indexOf("\"") >= 0) ||
                (s.indexOf("'") >= 0) || (s.indexOf("#") >= 0) || (s.indexOf("@") >= 0)
                || (s.indexOf(".") >= 0) || (s.indexOf("+") >= 0) || (s.indexOf("-") >= 0)
                || (s.indexOf("&") >= 0) || (s.indexOf("=") >= 0) || (s.indexOf("!") >= 0)
                || (s.indexOf("%") >= 0) || (s.indexOf("(") >= 0) || (s.indexOf(")") >= 0)
                || (s.indexOf("{") >= 0) || (s.indexOf("}") >= 0) || (s.indexOf("?") >= 0)
                || (s.indexOf(",") >= 0) || (s.indexOf("/") >= 0) || (s.indexOf("<") >= 0)
                || (s.indexOf(">") >= 0) || (s.indexOf(":") >= 0) || (s.indexOf(";") >= 0)
                || (s.indexOf("^") >= 0)) {
            intRet = -1;
        }
        return intRet;
    }

    public String verifyToken(String token) {
        try {
            log.info("BSS URL: " + bssAuthUrl);
            log.info("BSS Token: " + bssAuthToken);
            RestClient restTemplate = new RestClient();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + bssAuthToken);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            String postStr = "{\"token\":\"" + token + "\"}";
            String resultAsJsonStr = restTemplate.post(bssAuthUrl, postStr, headers);
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            String validate = convertedObject.get("validate").getAsString();
            if (validate.equals("true")) {
                return convertedObject.get("info").getAsJsonObject().get("email").getAsString();
            }

            return "";
        } catch (Exception e) {
            return "";
        }
    }

    public String getAppConfigValue(String name) {
        String valueStr;
        try {
            List<AppConfiguration> appConfigurations = dataManager.load(AppConfiguration.class).
                    query("select e from AppConfiguration e where e.name = :name")
                    .parameter("name", name).list();
            if (appConfigurations == null) {
                valueStr = "";
            } else {
                if (appConfigurations.size() == 0) {
                    valueStr = "";
                } else {
                    valueStr = appConfigurations.get(0).getConfigvalue();
                }
            }
        } catch (Exception e) {
            valueStr = "";
        }
        return valueStr;
    }

    public static int executeOsCommand(String command, StringBuilder outputCommand,
                                       StringBuilder errorCommand) {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");

        int retInt = 0;
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
        }

        try {
            Process process = builder.start();
            //StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                outputCommand.append(line + "\n");
            }

            //Error stream
            BufferedReader readerError = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));

            String lineError;
            while ((lineError = readerError.readLine()) != null) {
                errorCommand.append(lineError + "\n");
            }

            int exitVal = process.waitFor();
            retInt = exitVal;
        } catch (Exception exception) {
            retInt = -100;
            errorCommand.append(exception.getMessage());
        }
        return retInt;
    }

    public String getToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("Authorization", "Basic " + jmixClientBaseAuth);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("grant_type", "password");
            map.add("username", jmixAdminAccount);
            map.add("password", jmixAdminPassword);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
            String serviceUrl = jmixBaseUrl + "/oauth/token";

            ResponseEntity<String> result = restTemplate.postForEntity(serviceUrl, request, String.class);
            JsonObject convertedObject = new Gson().fromJson(result.getBody(), JsonObject.class);
            return convertedObject.get("access_token").getAsString();
        } catch (HttpClientErrorException httpClientErrorException) {
            return "";
        }
    }

    public static String getString(String accessToken, RestClient restTemplate, String searchBody, String searchGatewayUrl) {
        String searchResult = restTemplate.postToken(searchGatewayUrl, searchBody, "Authorization", "Bearer " + accessToken);
        JsonArray gateway = new Gson().fromJson(searchResult, JsonArray.class);

        if (gateway.size() > 0) {
            return gateway.get(0).getAsJsonObject().get("id").getAsString();
        }

        return "";
    }

    public static int createDnsEntry(String apiUri, String dnsHostName, HandledError handledError) {
        //return 200: OK
        if ((apiUri == null) || (dnsHostName == null)) {
            handledError.setErrorMsg("Parameters are NULL");
            return -1;
        }
        if (apiUri.isEmpty() || dnsHostName.isEmpty()) {
            handledError.setErrorMsg("Parameters are empty");
            return -2;
        }

        int retInt = -3;
        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            String postStr = "{\"name\":\"" + dnsHostName + "\"}";
            HttpEntity<String> request =
                    new HttpEntity<String>(postStr, headers);

            String resultAsJsonStr =
                    restTemplate.postForObject(apiUri, request, String.class);
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);

            retInt = 200; //OK
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -4;
        }
        return retInt;
    }

    public static int createRevProxy(String apiUri, String dnsHostName, HandledError handledError) {
        //return 200: OK
        if ((apiUri == null) || (dnsHostName == null)) {
            handledError.setErrorMsg("Parameters are NULL");
            return -1;
        }
        if (apiUri.isEmpty() || dnsHostName.isEmpty()) {
            handledError.setErrorMsg("Parameters are empty");
            return -2;
        }

        int retInt = -3;
        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            String postStr = "{\"name\":\"" + dnsHostName + "\"}";
            HttpEntity<String> request =
                    new HttpEntity<String>(postStr, headers);

            String resultAsJsonStr =
                    restTemplate.postForObject(apiUri, request, String.class);
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);

            retInt = 200; //OK
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            handledError.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -4;
        }
        return retInt;
    }

    public String createDrupalSite1(String provider, String catalog, String portalEndpoint,
                                   HandledError handledError){
        return "localhost:81/drupal2";
    }

    public String createDrupalSite(String provider, String catalog, String portalEndpoint,
                                   HandledError handledError){
        String result = "";
        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            String postStr = "{\n" +
                    "    \"provider\": \"" + provider + "\",\n" +
                    "\"catalog\":\"" + catalog + "\"\n" +
                    "}";
            //logger.info("Body create portal: " + postStr);
            HttpEntity<String> request = new HttpEntity<String>(postStr, headers);

            String createPortalUrl = portalEndpoint + "/createportal";
            String resultAsJsonStr = restTemplate.postForObject(createPortalUrl, request, String.class);
            JsonObject convertedObject = new Gson().fromJson(resultAsJsonStr, JsonObject.class);
            log.info("Response from create portal api: " + resultAsJsonStr);
            if (convertedObject.get("success").getAsBoolean()) {
                result = convertedObject.get("url").getAsString();
            }
            else {
                if (convertedObject.get("failed").getAsBoolean()) {
                    handledError.setErrorMsg(convertedObject.get("error").getAsString());
                    result = "";
                }
            }
        } catch (Exception e) {
            log.error("Error when create portal. Reason: " + e.getMessage());
            handledError.setErrorMsg(e.getMessage());
            result = "";
        }

        return result;
    }

        public String createApiNode(Site site, String siteUser, String sitePassword, RestApi api,
                                String portalEndpoint, DrupalLink drupalLink, HandledError handledError) {
        try {
            HttpHeaders headers = new HttpHeaders();
            //headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(("admin:" + basicAuth).getBytes()));
            headers.add("Authorization", "Basic " +
                    Base64.getEncoder().encodeToString((siteUser + ":" + sitePassword).getBytes()));
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            String apiEndpoint = site.getGateway().getInvocation_endpoint() + "/" + api.getOwner().getUsername().toLowerCase()
                    + "/" + site.getName().toLowerCase() + api.getBase_path().toLowerCase();
            if (Utility.CheckNullAndEmpty(api.getDocumentation())) {
                api.setDocumentation("No detailed infomation");
            }
            if (Utility.CheckNullAndEmpty(api.getTitle())) {
                api.setTitle(api.getName());
            }

            String body = "{  \n" +
                    "  \"type\": [\n" +
                    "    {\n" +
                    "      \"target_id\": \"api\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"title\": [\n" +
                    "      {\n" +
                    "          \"value\" : \"" + api.getTitle() + "\"\n" +
                    "      }\n" +
                    "  ],\n" +
                    "  \"status\": [\n" +
                    "      {\n" +
                    "          \"value\" : \"1\"\n" +
                    "      }\n" +
                    "  ],\n" +
                    "  \"field_api_endpoint\": [\n" +
                    "        {\n" +
                    "            \"value\": \"" + apiEndpoint + "\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "   \"field_api_name\": [\n" +
                    "        {\n" +
                    "            \"value\": \"" + api.getName() + "\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"field_api_documentation\": [\n" +
                    "        {\n" +
                    "            \"value\": \"" + api.getDocumentation() + "\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"field_api_header\": [\n" +
                    "        {\n" +
                    "            \"value\": \"application/json\" \n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"field_api_method\": [\n" +
                    "        {\n" +

                    "    ],\n" +
                    "    \"field_api_security_method\": [\n" +
                    "        {\n" +
                    "            \"value\": \"" + api.getSecurity_method().getName() + "\"\n" +
                    "        }\n" +        "            \"value\": \"POST\" \n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"path\": {\n" +
                    "        \"alias\": \"/api" + api.getBase_path() + "\"\n" +
                    "    }\n" +
                    "}";

            HttpEntity<String> entity = new HttpEntity<String>(body, headers);
            ResponseEntity<String> result = restTemplate.exchange(portalEndpoint + "/node?_format=json", HttpMethod.POST, entity, String.class);

            JsonObject convertedObject = new Gson().fromJson(result.getBody(), JsonObject.class);
            String apiDrupalId = convertedObject.get("nid").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
            String uuid = convertedObject.get("uuid").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
            drupalLink.setDrupal_api_id(apiDrupalId);
            dataManager.save(drupalLink);
            return "{\n" +
                    "            \"target_id\": " + apiDrupalId + ",\n" +
                    "            \"target_type\": \"node\",\n" +
                    "            \"target_uuid\": \"" + uuid + "\",\n" +
                    "            \"url\": \"/node/" + apiDrupalId + "\"\n" +
                    "        }";
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
        }

        return "";
    }

    public String updateApiNode(Site site, String siteUser, String sitePassword, RestApi api,
                                String portalEndpoint, DrupalLink drupalLink, HandledError handledError) {
        try {
            HttpHeaders headers = new HttpHeaders();
            //headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(("admin:" + basicAuth).getBytes()));
            headers.add("Authorization", "Basic " +
                    Base64.getEncoder().encodeToString((siteUser + ":" + sitePassword).getBytes()));
            RestTemplate restTemplate = new RestTemplate();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setConnectTimeout(TIMEOUT);
            requestFactory.setReadTimeout(TIMEOUT);

            restTemplate.setRequestFactory(requestFactory);
            String apiEndpoint = site.getGateway().getInvocation_endpoint() + "/" + api.getOwner().getUsername().toLowerCase()
                    + "/" + site.getName().toLowerCase() + api.getBase_path().toLowerCase();

            if (Utility.CheckNullAndEmpty(api.getDocumentation())) {
                api.setDocumentation("No detailed infomation");
            }
            if (Utility.CheckNullAndEmpty(api.getTitle())) {
                api.setTitle(api.getName());
            }

            String body = "{  \n" +
                    "  \"type\": [\n" +
                    "    {\n" +
                    "      \"target_id\": \"api\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"title\": [\n" +
                    "      {\n" +
                    "          \"value\" : \"" + api.getTitle() + "\"\n" +
                    "      }\n" +
                    "  ],\n" +
                    "  \"status\": [\n" +
                    "      {\n" +
                    "          \"value\" : \"1\"\n" +
                    "      }\n" +
                    "  ],\n" +
                    "  \"field_api_endpoint\": [\n" +
                    "        {\n" +
                    "            \"value\": \"" + apiEndpoint + "\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "   \"field_api_name\": [\n" +
                    "        {\n" +
                    "            \"value\": \"" + api.getName() + "\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"field_api_documentation\": [\n" +
                    "        {\n" +
                    "            \"value\": \"" + api.getDocumentation() + "\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"field_api_header\": [\n" +
                    "        {\n" +
                    "            \"value\": application/json \n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"field_api_method\": [\n" +
                    "        {\n" +
                    "            \"value\": POST \n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"field_api_security_method\": [\n" +
                    "        {\n" +
                    "            \"value\": \"" + api.getSecurity_method().getName() + "\"\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";

            HttpEntity<String> entity = new HttpEntity<String>(body, headers);
            ResponseEntity<String> result = restTemplate.exchange(portalEndpoint + "/node/" + drupalLink.getDrupal_api_id() + "?_format=json", HttpMethod.PATCH, entity, String.class);
            return "";
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
        }

        return "";
    }

    public String getApiNode(String siteUser, String sitePassword, String portalEndpoint, String nodeId,
                             HandledError handledError) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " +
                    Base64.getEncoder().encodeToString((siteUser + ":" + sitePassword).getBytes()));
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> entity = new HttpEntity<String>("", headers);
            ResponseEntity<String> result = restTemplate.exchange(portalEndpoint + "/node/" + nodeId + "?_format=json", HttpMethod.GET, entity, String.class);
            JsonObject convertedObject = new Gson().fromJson(result.getBody(), JsonObject.class);
            String uuid = convertedObject.get("uuid").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
            return "{\n" +
                    "            \"target_id\": " + nodeId + ",\n" +
                    "            \"target_type\": \"node\",\n" +
                    "            \"target_uuid\": \"" + uuid + "\",\n" +
                    "            \"url\": \"/node/" + nodeId + "\"\n" +
                    "        }";
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
        }

        return "";
    }

    public static Boolean updateProductNode(String siteUser, String sitePassword, String drupalUrl,
                                            String bodyUpdate, String productDrupalId, Product product, HandledError handledError) {
        try {
            HttpHeaders headers = new HttpHeaders();
            //headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(("admin:" + basicAuth).getBytes()));
            headers.add("Authorization", "Basic " +
                    Base64.getEncoder().encodeToString((siteUser + ":" + sitePassword).getBytes()));
            RestTemplate restTemplate = new RestTemplate();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setConnectTimeout(TIMEOUT);
            requestFactory.setReadTimeout(TIMEOUT);

            restTemplate.setRequestFactory(requestFactory);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            String bodyProduct = "{  \n" +
                    "  \"type\": [\n" +
                    "    {\n" +
                    "      \"target_id\": \"product\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "   \"title\" : [\n" +
                    "      {\n" +
                    "        \"value\":\"" + product.getTitle() + "\"\n" +
                    "      }\n" +
                    "   ]\n";

            if (!bodyUpdate.isEmpty()) {
                bodyProduct = bodyProduct + ",\"field_apis_in_this_product\":[" + bodyUpdate + "]}";
            }
            HttpEntity<String> entity = new HttpEntity<String>(bodyProduct, headers);
            ResponseEntity<String> response = restTemplate.exchange(drupalUrl + "/node/" + productDrupalId + "?_format=json", HttpMethod.PATCH, entity, String.class);
            return false;
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
        }

        return true;
    }

    public Boolean createProductNode(String siteUser, String sitePassword, String drupalUrl,
                                     String bodyUpdate, PublishedProduct publishedProduct, HandledError handledError) {
        try {
            HttpHeaders headers = new HttpHeaders();
            //headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(("admin:" + basicAuth).getBytes()));
            headers.add("Authorization", "Basic " +
                    Base64.getEncoder().encodeToString((siteUser + ":" + sitePassword).getBytes()));
            RestTemplate restTemplate = new RestTemplate();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setConnectTimeout(TIMEOUT);
            requestFactory.setReadTimeout(TIMEOUT);

            restTemplate.setRequestFactory(requestFactory);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            String titleStr;
            if (Utility.CheckNullAndEmpty(publishedProduct.getProduct().getTitle())) {
                titleStr = "";
            }
            else {
                titleStr = publishedProduct.getProduct().getTitle();
            }

            String bodyProduct = "{  \n" +
                    "  \"type\": [\n" +
                    "    {\n" +
                    "      \"target_id\": \"product\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "   \"title\" : [\n" +
                    "      {\n" +
                    "        \"value\":\"" + publishedProduct.getProduct().getName() + "\"\n" +
                    "      }\n" +
                    "   ],\n" +
                    "   \"body\" : [\n" +
                    "      {\n" +
                    "        \"value\":\"" + titleStr + "\"\n" +
                    "      }\n " +
                    "   ],\n" +
                    "   \"path\": {\n" +
                    "        \"alias\": \"/product/" + publishedProduct.getProduct().getName() + "\"\n" +
                    "   }\n";

            if (!bodyUpdate.isEmpty()) {
                bodyProduct = bodyProduct.substring(0, bodyProduct.length() - 1) + ",\n" +
                        "\"field_apis_in_this_product\":[" + bodyUpdate + "]\n}";
            } else {
                bodyProduct = bodyProduct + "}";
            }
            log.info("Body create product: " + bodyProduct);
            HttpEntity<String> entity = new HttpEntity<String>(bodyProduct, headers);
            ResponseEntity<String> response = restTemplate.exchange(drupalUrl + "/node?_format=json", HttpMethod.POST, entity, String.class);

            JsonObject convertedObject = new Gson().fromJson(response.getBody(), JsonObject.class);
            int apiDrupalId = convertedObject.get("nid").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsInt();
            publishedProduct.setDrupal_product_id(apiDrupalId);
            dataManager.save(publishedProduct);
            return false;
        } catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
        }

        return true;
    }

    public static int deleteNode(String siteUser, String sitePassword, String portalEndpoint, String nodeId,
                                     HandledError handledError) {
        int retInt;
        try {
            HttpHeaders headers = new HttpHeaders();
            //headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(("admin:" + basicAuth).getBytes()));
            headers.add("Authorization", "Basic " +
                    Base64.getEncoder().encodeToString((siteUser + ":" + sitePassword).getBytes()));
            RestTemplate restTemplate = new RestTemplate();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setConnectTimeout(TIMEOUT);
            requestFactory.setReadTimeout(TIMEOUT);

            restTemplate.setRequestFactory(requestFactory);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<String>(null, headers);
            ResponseEntity<String> response = restTemplate.exchange(portalEndpoint + "/node/" + nodeId + "?_format=json", HttpMethod.DELETE, entity, String.class);
            retInt = response.getStatusCodeValue();
        } catch (HttpClientErrorException httpClientErrorException) {
            handledError.setErrorMsg(httpClientErrorException.getMessage());
            retInt = httpClientErrorException.getRawStatusCode();
        }  catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            retInt = -1;
        }
        return retInt;
    }

    public static String encodeBase64(String inputStr) {
        if (inputStr == null) {
            return "";
        }
        if (inputStr.equals("")) {
            return "";
        }
        return Base64.getEncoder().encodeToString(inputStr.getBytes());
    }

    public static String decodeBase64(String encodedStr) {
        if (encodedStr == null) {
            return "";
        }
        if (encodedStr.equals("")) {
            return "";
        }
        String decodedString;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedStr);
            decodedString = new String(decodedBytes);
        } catch (Exception e) {
            decodedString = "";
        }
        return decodedString;
    }

    public boolean validateSchema(String schema, HandledError handledError) {
        try {
            if (schema == null) {
                handledError.setErrorMsg("Missing content for body");
                return false;
            }

//            JsonObject jsonObject = new Gson().fromJson(schema, JsonObject.class);
        } catch (JsonSyntaxException exception) {
            handledError.setErrorMsg(exception.getMessage());
            return false;
        }

        return true;
    }

    public boolean validateBodySchema(String schema, HandledError handledError) {
        try {
            if (schema == null) {
                handledError.setErrorMsg("Missing content for body");
                return false;
            }

            JsonObject jsonObject = new Gson().fromJson(schema, JsonObject.class);
            JsonElement properties = jsonObject.get("properties");
            if (properties == null) {
                handledError.setErrorMsg("Missing properties in body_schema");
                return false;
            }
        } catch (JsonSyntaxException exception) {
            handledError.setErrorMsg(exception.getMessage());
            return false;
        }

        return true;
    }

    public static List<String> vpToLst(ValuesPicker<String> vp) {
        return vp.getValue() == null ? new ArrayList<String>() : vp.getValue().stream().toList();
    }

    public static ArrayList<String> vpToArrLst(ValuesPicker<String> vp) {
        return vp.getValue() == null ? new ArrayList<String>() : new ArrayList<String>(vp.getValue().stream().toList());
    }

    public static void addElementToValuesPicker(ValuesPicker v, String value) {
        ArrayList<String> lst = Utility.vpToLst(v) == null ? new ArrayList<String>() : new ArrayList<>(Utility.vpToLst(v));
        if (lst.contains(value)) {
            return;
        } else {
            lst.add(value);
            v.setValue(lst);
        }
    }

    public void writeFile(String fileName, String content) throws IOException {
        File logDirectory = new File(TEMP_DIRECTORY + "/log-s3");

        if (!logDirectory.exists()) {
            if (!logDirectory.mkdir()){
                return;
            }
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(logDirectory.getPath() + "/" + fileName, true));
        writer.append(content);
        writer.close();
    }

}