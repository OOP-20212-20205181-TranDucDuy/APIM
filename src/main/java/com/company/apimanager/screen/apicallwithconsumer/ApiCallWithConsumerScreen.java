package com.company.apimanager.screen.apicallwithconsumer;

import com.company.apimanager.app.EncodePassword;
import com.company.apimanager.app.Utility;
import com.company.apimanager.entity.*;
import com.company.apimanager.screen.brandlogin.ApiRegister;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.TableItems;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.management.Notification;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import com.company.apimanager.app.EncodePassword;
import com.company.apimanager.entity.ApiHttpHeader;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@UiController("ApiCallWithConsumerScreen")
@UiDescriptor("api-call-with-consumer-screen.xml")
public class ApiCallWithConsumerScreen extends Screen {
    @Autowired
    private ComboBox<String> authorization_typeField;
    @Autowired
    private ComboBox algorithmField;
    @Autowired
    private ComboBox<String> methodField;
    @Autowired
    private TextField authenParam1;
    @Autowired
    private TextField authenParam2;
    @Autowired
    private TextArea jwtHeadersField;
    @Autowired
    private TextArea payloadField;
    @Autowired
    private EncodePassword encodePassword;
    @Autowired
    private CheckBox encodedField;
    @Autowired
    private Table<ApiHttpHeader> httpHeadersTable;
    @Autowired
    private CollectionContainer<ApiHttpHeader> httpHeadersDc;

    private Collection<ApiHttpHeader> headerCollection;
    @Autowired
    private Notifications notifications;
    @Autowired
    private TextArea<String> request_bodyField;
    @Autowired
    private TextField<String> endpointField;
    @Autowired
    private TextArea responseField;
    @Autowired
    private Action windowClose;

    private String apiEndpoint;
    @Autowired
    private DataManager dataManager;

    private Site apiSite;

    @Autowired
    private EntityComboBox consumerField;
    @Autowired
    private CollectionLoader<Consumer> consumersDl;
    @Autowired
    private CurrentAuthentication currentAuthentication;


    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {

        consumersDl.setParameter("site", apiSite);

        List<String> authenMethodOptions = new ArrayList<>();
        authenMethodOptions.add("Basic");
        authenMethodOptions.add("API key");
        authenMethodOptions.add("Bearer token");
        authenMethodOptions.add("JWT");
        authorization_typeField.setOptionsList(authenMethodOptions);

        List<String> algorithmOptions = new ArrayList<>();
        algorithmOptions.add("HS256");
        algorithmOptions.add("HS384");
        algorithmOptions.add("HS512");
        algorithmOptions.add("RS256");
        algorithmOptions.add("RS384");
        algorithmOptions.add("HS512");
        algorithmOptions.add("HS256");
        algorithmOptions.add("PS384");
        algorithmOptions.add("PS512");
        algorithmOptions.add("ES256");
        algorithmOptions.add("ES384");
        algorithmOptions.add("ES512");

        algorithmField.setOptionsList(algorithmOptions);

        List<String> httpMethodOptions = new ArrayList<>();
        httpMethodOptions.add("GET");
        httpMethodOptions.add("POST");
        httpMethodOptions.add("PUT");
        httpMethodOptions.add("DELETE");

        methodField.setOptionsList(httpMethodOptions);

        authenParam1.setVisible(false);
        authenParam2.setVisible(false);

        algorithmField.setVisible(false);
        jwtHeadersField.setVisible(false);
        payloadField.setVisible(false);
        encodedField.setVisible(false);

        headerCollection = new ArrayList<>();

        ApiHttpHeader apiHttpHeader = new ApiHttpHeader();
        apiHttpHeader.setHeader_name("Accept");
        apiHttpHeader.setHeader_value("*/*");

        ApiHttpHeader apiHttpHeader1 = new ApiHttpHeader();
        apiHttpHeader1.setHeader_name("Content-Type");
        apiHttpHeader1.setHeader_value("application/json");
        headerCollection.add(apiHttpHeader1);

        endpointField.setValue(apiEndpoint);

        headerCollection.add(apiHttpHeader);
        try {
            httpHeadersDc.setItems(headerCollection);
        }catch (Exception e) {
            notifications.create().withType(Notifications.NotificationType.ERROR).withCaption(e.getMessage()).show();
        }
    }

    @Subscribe("authorization_typeField")
    public void onAuthorization_typeFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        String authoType = authorization_typeField.getValue();
        if (authoType == null) {
            authenParam1.setVisible(false);
            authenParam2.setVisible(false);

            algorithmField.setVisible(false);
            jwtHeadersField.setVisible(false);
            payloadField.setVisible(false);
            encodedField.setVisible(false);

            authenParam1.clear();
            authenParam2.clear();
            algorithmField.clear();
            jwtHeadersField.clear();
            payloadField.clear();
            encodedField.clear();
            return;
        }
        if (authoType.equals("Basic")) {
            authenParam1.setVisible(true);
            authenParam2.setVisible(true);

            authenParam1.setCaption("User");
            authenParam2.setCaption("Password");

            algorithmField.setVisible(false);
            jwtHeadersField.setVisible(false);
            payloadField.setVisible(false);
            encodedField.setVisible(false);

            authenParam1.clear();
            authenParam2.clear();
            algorithmField.clear();
            jwtHeadersField.clear();
            payloadField.clear();
            encodedField.clear();

            //Consumer
            Consumer currConsumer = (Consumer) consumerField.getValue();
            if (currConsumer != null) {
                authenParam1.setValue((currentAuthentication.getUser().getUsername() + "." + apiSite.getName() + "." + currConsumer.getName()).toLowerCase());
            }

            return;
        }
        if (authoType.equals("API key")) {
            authenParam1.setVisible(true);
            authenParam2.setVisible(true);

            authenParam1.setCaption("Key name");
            authenParam2.setCaption("Key value");

            algorithmField.setVisible(false);
            jwtHeadersField.setVisible(false);
            payloadField.setVisible(false);
            encodedField.setVisible(false);

            authenParam1.clear();
            authenParam2.clear();
            algorithmField.clear();
            jwtHeadersField.clear();
            payloadField.clear();
            encodedField.clear();

            //Consumer
            Consumer currConsumer = (Consumer) consumerField.getValue();
            authenParam1.setValue("api-key");
            if (currConsumer != null) {
                authenParam2.setValue(currConsumer.getApi_key());
            }

            return;
        }
        if (authoType.equals("Bearer token")) {
            authenParam1.setVisible(true);
            authenParam2.setVisible(false);

            authenParam1.setCaption("Token");

            algorithmField.setVisible(false);
            jwtHeadersField.setVisible(false);
            payloadField.setVisible(false);
            encodedField.setVisible(false);

            authenParam1.clear();
            authenParam2.clear();
            algorithmField.clear();
            jwtHeadersField.clear();
            payloadField.clear();
            encodedField.clear();

            return;
        }
        if (authoType.equals("JWT")) {
            authenParam1.setVisible(true);
            authenParam2.setVisible(true);

            authenParam1.setCaption("Secret");
            authenParam2.setCaption("Header prefix");
            authenParam2.setValue("Bearer");

            algorithmField.setVisible(true);
            jwtHeadersField.setVisible(true);
            payloadField.setVisible(true);
            encodedField.setVisible(true);

            authenParam1.clear();
            authenParam2.clear();
            algorithmField.clear();
            jwtHeadersField.clear();
            payloadField.clear();
            encodedField.clear();

            return;
        }
        authenParam1.setVisible(false);
        authenParam2.setVisible(false);

        algorithmField.setVisible(false);
        jwtHeadersField.setVisible(false);
        payloadField.setVisible(false);
        encodedField.setVisible(false);

        authenParam1.clear();
        authenParam2.clear();
        algorithmField.clear();
        jwtHeadersField.clear();
        payloadField.clear();
        encodedField.clear();
    }

    @Subscribe("methodField")
    public void onMethodFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        String httpMethod = methodField.getValue();
        if (httpMethod == null) {
            return;
        }
        if ((httpMethod.equals("GET")) || ((httpMethod.equals("DELETE")))) {
            request_bodyField.clear();
            request_bodyField.setEnabled(false);
        }
        else {
            request_bodyField.clear();
            request_bodyField.setEnabled(true);
        }
    }

    @Subscribe("consumerField")
    public void onConsumerFieldValueChange(HasValue.ValueChangeEvent<Consumer> event) {
        Consumer currConsumer = event.getValue();
        if (currConsumer == null) {
            return;
        }
        if (authorization_typeField.getValue() == null) {
            return;
        }
        if (authorization_typeField.getValue().equals("Basic")) {
            authenParam1.setValue((currentAuthentication.getUser().getUsername() + "." + apiSite.getName() + "." + currConsumer.getName()).toLowerCase());
            return;
        }
        if (authorization_typeField.getValue().equals("API key")) {
            authenParam1.setValue("api-key");
            authenParam2.setValue(currConsumer.getApi_key());
            return;
        }
    }



    @Subscribe("callBtn")
    public void onCallBtnClick(Button.ClickEvent event) {
        List<ApiHttpHeader> apiHeaders =  httpHeadersDc.getItems();
        if (apiHeaders == null) {
            notifications.create().withType(Notifications.NotificationType.ERROR).withCaption("No request header").show();
            return;
        }
        if (apiHeaders.size() == 0) {
            notifications.create().withType(Notifications.NotificationType.ERROR).withCaption("No request header").show();
            return;
        }

        if (endpointField.getValue() == null) {
            notifications.create().withType(Notifications.NotificationType.ERROR).withCaption("API endpoint empty").show();
            return;
        }

        if (endpointField.getValue().isEmpty()) {
            notifications.create().withType(Notifications.NotificationType.ERROR).withCaption("API endpoint empty").show();
            return;
        }

        String httpMethod = methodField.getValue();
        if (httpMethod == null) {
            notifications.create().withType(Notifications.NotificationType.ERROR).withCaption("Please select HTTP method: GET/POST/PUT/DELETE").show();
            methodField.focus();
            return;
        }
        if (httpMethod.equals("GET")) {
            int rs = this.doGet();
            //notifications.create().withType(Notifications.NotificationType.HUMANIZED).withCaption("RS: " + rs).show();
            return;
        }
        if (httpMethod.equals("POST")) {
            this.doPost();
            return;
        }
        if (httpMethod.equals("PUT")) {
            this.doPut();
            return;
        }
        if (httpMethod.equals("DELETE")) {
            this.doDelete();
            return;
        }
        notifications.create().withType(Notifications.NotificationType.ERROR).withCaption("Please select HTTP method: GET/POST/PUT/DELETE").show();
        methodField.focus();
    }

    @Subscribe("closeBtn")
    public void onCloseBtnClick(Button.ClickEvent event) {
        close(StandardOutcome.CLOSE);
    }

    @Autowired
    private Action windowCommitAndClose;

    private int doPost() {
        int intRs = 0;
        String response;
        String requestBody = request_bodyField.getRawValue();
        if (requestBody == null) {
            requestBody = "";
        }
        if (requestBody.isEmpty()) {
            notifications.create().withType(Notifications.NotificationType.ERROR).withCaption("Request body empty").show();
            return -1;
        }

        RestTemplate restTemplate = new RestTemplate();
        try {
            String endpointApi = endpointField.getValue();
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> requestPost = new HttpEntity<String>(requestBody, headers);
            this.setHeaders(headers);
            this.setAuthenHeader(headers);
            response = restTemplate.postForObject(endpointApi, requestPost, String.class);
        }
        catch (HttpClientErrorException httpClientErrorException) {
            response = httpClientErrorException.getMessage();
            intRs = -1;
        }
        catch (Exception exception) {
            response = exception.getMessage();
            intRs = -1;
        }
        responseField.setValue(response);
        return intRs;
    }

    private int doPut() {
        int intRs = 0;
        String response;
        String requestBody = request_bodyField.getRawValue();
        if (requestBody == null) {
            requestBody = "";
        }
        if (requestBody.isEmpty()) {
            notifications.create().withType(Notifications.NotificationType.ERROR).withCaption("Request body empty").show();
            return -1;

        }

        RestTemplate restTemplate = new RestTemplate();
        try {
            String endpointApi = endpointField.getValue();
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> requestPost = new HttpEntity<String>(requestBody, headers);
            this.setHeaders(headers);
            this.setAuthenHeader(headers);
            response = restTemplate.exchange(endpointApi, HttpMethod.PUT, requestPost, String.class).getBody();
        }
        catch (HttpClientErrorException httpClientErrorException) {
            response = httpClientErrorException.getMessage();
            intRs = -1;
        }
        catch (Exception exception) {
            response = exception.getMessage();
            intRs = -1;
        }
        responseField.setValue(response);
        return intRs;
    }
    private int doGet() {
        int intRs = 0;
        String response;
        RestTemplate restTemplate = new RestTemplate();

        try {
            String endpointApi = endpointField.getValue();
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
            this.setHeaders(headers);

            this.setAuthenHeader(headers);
            response = restTemplate.exchange(endpointApi, HttpMethod.GET, httpEntity, String.class).getBody();
        }
        catch (HttpClientErrorException httpClientErrorException) {
            response = httpClientErrorException.getMessage();
            intRs = httpClientErrorException.getRawStatusCode();
        }
        catch (Exception exception) {
            response = exception.getMessage();
            intRs = -1;
        }

        responseField.setValue(response);
        return intRs;
    }

    private int doDelete() {
        int intRs = 0;
        String response;
        RestTemplate restTemplate = new RestTemplate();

        try {
            String endpointApi = endpointField.getValue();
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
            this.setHeaders(headers);

            this.setAuthenHeader(headers);
            response = restTemplate.exchange(endpointApi, HttpMethod.DELETE, httpEntity, String.class).getBody();
        }
        catch (HttpClientErrorException httpClientErrorException) {
            response = httpClientErrorException.getMessage();
            intRs = -1;
        }
        catch (Exception exception) {
            response = exception.getMessage();
            intRs = -1;
        }

        responseField.setValue(response);
        return intRs;
    }

    private int setAuthenHeader(HttpHeaders httpHeaders) {
        String authoType = authorization_typeField.getValue();
        if (authoType == null) {
            return 0;
        }
        if (authoType.isEmpty()) {
            return 0;
        }
        String param1 = authenParam1.getRawValue();
        if (param1 == null) {
            param1 = "";
        }
        String param2 = authenParam2.getRawValue();
        if (param2 == null) {
            param2 = "";
        }

        if (authoType.equals("Basic")) {
            int rsInt;
            if ((param1.isEmpty()) || (param2.isEmpty())) {
                notifications.create().withType(Notifications.NotificationType.ERROR).withCaption("User or Password empty").show();
                rsInt = -1;
            }
            else {
                httpHeaders.setBasicAuth(param1, param2);
                rsInt = 0;
            }
            return rsInt;
        }
        if (authoType.equals("API key")) {
            int rsInt;
            if ((param1.isEmpty()) || (param2.isEmpty())) {

                notifications.create().withType(Notifications.NotificationType.ERROR).withCaption("Key name or key value empty").show();
                rsInt = -1;
            }
            else {
                httpHeaders.set(param1, param2);
                rsInt = 0;
            }
            return rsInt;
        }
        if (authoType.equals("Bearer token")) {
            int rsInt;
            if (param1.isEmpty()) {
                notifications.create().withType(Notifications.NotificationType.ERROR).withCaption("Token empty").show();
                rsInt = -1;
            }
            else {
                httpHeaders.setBearerAuth(param1);
                rsInt = 0;
            }
            return rsInt;
        }
        notifications.create().withType(Notifications.NotificationType.ERROR).withCaption("Unknown authorization type").show();
        return -1;
    }

    private void setHeaders(HttpHeaders httpHeaders) {
        List<ApiHttpHeader> apiHeaders =  httpHeadersDc.getItems();
        for (int i = 0; i < apiHeaders.size(); i++) {
            httpHeaders.set(apiHeaders.get(i).getHeader_name(), apiHeaders.get(i).getHeader_value());
        }
    }
    public void setApiEndpoint(String endpoint) {
        apiEndpoint = endpoint;
    }

    public void setApiSite(Site site) {
        this.apiSite = site;
    }
}