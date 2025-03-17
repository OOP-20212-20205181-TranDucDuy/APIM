package com.company.apimanager.screen.apicall;

import com.company.apimanager.app.EncodePassword;
import com.company.apimanager.app.Utility;
import com.company.apimanager.entity.ApiHttpHeader;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.TableItems;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.ApiCall;
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

@UiController("ApiCall.edit")
@UiDescriptor("api-call-edit.xml")
@EditedEntityContainer("apiCallDc")
public class ApiCallEdit extends StandardEditor<ApiCall> {
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

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (apiEndpoint != null) {
            boolean noProtolcol = false;
            if (apiEndpoint.length() < 9) { // http://
                noProtolcol = true;
            }
            else {
                String protocolStr = apiEndpoint.substring(0, 4).toLowerCase();
                if (!protocolStr.equals("http")) {
                    noProtolcol = true;
                }
                else {
                    String fullProtocol = apiEndpoint.substring(0, 7).toLowerCase();
                    if (fullProtocol.equals("http://")) {
                        noProtolcol = false;
                    }
                    else {
                        String fullProtocols = apiEndpoint.substring(0, 8).toLowerCase();
                        if (fullProtocols.equals("https://")) {
                            noProtolcol = false;
                        }
                    }
                }
            }
            if (noProtolcol) {
                apiEndpoint = "https://" + apiEndpoint;
            }
        }

        endpointField.setValue(apiEndpoint);
    }



    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
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
        headerCollection.add(apiHttpHeader);

        ApiHttpHeader apiHttpHeader1 = new ApiHttpHeader();
        apiHttpHeader1.setHeader_name("Content-Type");
        apiHttpHeader1.setHeader_value("application/json");
        headerCollection.add(apiHttpHeader1);

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

    }

    @Autowired
    private Action windowCommitAndClose;

    @Subscribe("clearBtn")
    public void onClearBtnClick(Button.ClickEvent event) {
        authenParam1.clear();
        authenParam2.clear();
        methodField.clear();
        endpointField.clear();
        authorization_typeField.clear();
        request_bodyField.clear();
        responseField.clear();
    }

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
}