package com.company.apimanager.screen.importapis;

import com.company.apimanager.app.Utility;
import com.company.apimanager.entity.ApiSecurityMethod;
import com.company.apimanager.entity.DtoRestApi;
import com.company.apimanager.entity.RestApi;
import com.company.apimanager.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.EntityImportExport;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.data.exception.UniqueConstraintViolationException;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

@UiController("ImportApisScreen")
@UiDescriptor("import_apis-screen.xml")
public class ImportApisScreen extends Screen {
    @Autowired
    private EntityImportExport entityImportExport;
    @Autowired
    private TextArea jsonField;
    @Autowired
    private Notifications notifications;
    @Autowired
    private CollectionContainer<DtoRestApi> dtoRestApiDc;
    @Autowired
    private GroupTable<DtoRestApi> importedRestApisTable;
    @Autowired
    private Button parseBtn;
    @Autowired
    private Button importBtn;
    @Autowired
    private CollectionContainer<DtoRestApi> importedDtoRestApiDc;
    @Autowired
    private DataManager dataManager;

    private List<ApiSecurityMethod> apiSecurityMethodList;

    private ApiSecurityMethod basicSecurityMethod;
    private ApiSecurityMethod apiKeySecurityMethod;
    private ApiSecurityMethod oauth2SecurityMethod;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private TextField errorLogField;
    @Autowired
    private GroupTable<DtoRestApi> restApisTable;
    @Autowired
    private Button backBtn;

    private boolean remainApi;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        //importedRestApisTable.setEnabled(false);
        parseBtn.setEnabled(false);
        importBtn.setEnabled(false);
        backBtn.setEnabled(false);

        apiSecurityMethodList = dataManager.load(ApiSecurityMethod.class).all().list();
        if (apiSecurityMethodList != null) {
            for (int i = 0; i < apiSecurityMethodList.size(); i++) {
                if (apiSecurityMethodList.get(i).getName().equals("Basic")) {
                    basicSecurityMethod = apiSecurityMethodList.get(i);
                }
                if (apiSecurityMethodList.get(i).getName().equals("API key")) {
                    apiKeySecurityMethod = apiSecurityMethodList.get(i);
                }
                if (apiSecurityMethodList.get(i).getName().equals("OAuth")) {
                    oauth2SecurityMethod = apiSecurityMethodList.get(i);
                }
            }
        }
        if ((basicSecurityMethod == null) || (apiKeySecurityMethod == null) || (oauth2SecurityMethod == null)) {
            notifications.create(Notifications.NotificationType.ERROR).withCaption("Error when loading security methods").show();
            jsonField.setEnabled(false);
            parseBtn.setEnabled(false);
            importBtn.setEnabled(false);
        }

        remainApi = false;
    }

    @Subscribe("jsonField")
    public void onJsonFieldValueChange(HasValue.ValueChangeEvent event) {
        if (event.getValue() == null) {
            parseBtn.setEnabled(false);
            return;
        }
        if (event.getValue().toString().isEmpty()) {
            parseBtn.setEnabled(false);
        }
        else {
            parseBtn.setEnabled(true);
        }
    }


    @Subscribe("parseBtn")
    public void onParseBtnClick(Button.ClickEvent event) {
        String jsonText = jsonField.getRawValue();
        if (jsonText == null) {
            return;
        }
        if (jsonText.isEmpty()) {
            return;
        }

        boolean convertOK = true;
        String errMsg = "";
        List<DtoRestApi> restApiList = new ArrayList<DtoRestApi>();

        String errParse = "";

        try {
            JsonArray convertedObjects = new Gson().fromJson(jsonText, JsonArray.class);
            if (convertedObjects != null) {
                for (int i = 0; i < convertedObjects.size(); i++) {
                    StringBuilder errStr = new StringBuilder("");
                    DtoRestApi dtoRestApi = jsonObjectToApi(convertedObjects.get(i).getAsJsonObject(), errStr);
                    if (dtoRestApi != null) {
                        restApiList.add(dtoRestApi);
                    }
                    else {
                        String tt;
                        int j = i + 1;
                        
                        if (j == 1) {
                            tt = "st";
                        }
                        else {
                            if (j < 4) {
                                tt = "rd";
                            }
                            else {
                                tt = "th";
                            }
                        }
                        errParse = errParse + "\r\n" + "Cloud not parse " + j +  tt + " element: "
                                + errStr.toString();
                    }
                }
            }
        } catch (Exception exception) {
            convertOK = false;
            errMsg = exception.getMessage();
        }
        if (!convertOK) {
            notifications.create(Notifications.NotificationType.ERROR).withCaption(errMsg).show();
            return;
        }

        if (!errParse.isEmpty()) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption(errParse).show();
        }

        Collection<DtoRestApi> apiCollection = new ArrayList<>();
        for (int i = 0; i < restApiList.size(); i++) {
            apiCollection.add(restApiList.get(i));
        }
        dtoRestApiDc.setItems(apiCollection);
        if (restApiList.size() > 0) {
            importBtn.setEnabled(true);
            jsonField.setEnabled(false);
            backBtn.setEnabled(true);
            parseBtn.setEnabled(false);
        }
        remainApi = false;
    }

    @Subscribe("backBtn")
    public void onBackBtnClick(Button.ClickEvent event) {
        importBtn.setEnabled(false);
        jsonField.setEnabled(true);
        dtoRestApiDc.setItems(null);
        backBtn.setEnabled(false);
        parseBtn.setEnabled(true);
        importedDtoRestApiDc.setItems(null);
        errorLogField.clear();
        remainApi = false;
    }

    @Subscribe("importBtn")
    public void onImportBtnClick(Button.ClickEvent event) {
        if (dtoRestApiDc.getItems() == null) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("No importing APIs").show();
            return;
        }
        if (dtoRestApiDc.getItems().size() == 0) {
            notifications.create(Notifications.NotificationType.WARNING).withCaption("No importing APIs").show();
            return;
        }
        Collection<DtoRestApi> importedApiCollection = new ArrayList<>();
        List<DtoRestApi> importingApis = dtoRestApiDc.getItems();
        String removedIndex[] = new String[dtoRestApiDc.getItems().size()];

        List<String> errorList = new ArrayList<>();

        for (int i = 0; i < importingApis.size(); i++) {
            StringBuilder errMsg = new StringBuilder();
            if (importSingleApi(importingApis.get(i), errMsg) == 0) {
                importedApiCollection.add(importingApis.get(i));
                removedIndex[i] = "";
            }
            else {
                if (errMsg == null) {
                    removedIndex[i] = "Unknown error";
                }
                else {
                    removedIndex[i] = errMsg.toString();
                }
            }
        }
        Collection<DtoRestApi> newImportingCollection = new ArrayList<>();

        for (int i = 0; i < removedIndex.length; i++) {
            if (!removedIndex[i].isEmpty()) {
                importingApis.get(i).setImportError(removedIndex[i]);
                newImportingCollection.add(importingApis.get(i));
                errorList.add(removedIndex[i]);
            }
        }

        Collection<DtoRestApi> apiCollection = new ArrayList<>();
        for (int i = 0; i < importingApis.size(); i++) {
            apiCollection.add(importingApis.get(i));
        }
        dtoRestApiDc.setItems(newImportingCollection);
        importedDtoRestApiDc.setItems(importedApiCollection);
        if (newImportingCollection.isEmpty()) {
            remainApi = false;
        }
        else {
            remainApi = true;
        }
        importBtn.setEnabled(false);
        importedRestApisTable.focus();

        if (restApisTable.getSingleSelected() != null) {
            if (restApisTable.getSingleSelected().getImportError() != null) {
                errorLogField.setValue(restApisTable.getSingleSelected().getImportError());
            }
            else {
                errorLogField.setValue("");
            }
        }
    }

    @Install(to = "restApisTable", subject = "styleProvider")
    private String restApisTableStyleProvider(DtoRestApi entity, String property) {
        if (remainApi) {
            return "level-gold";
        }
        else {
            return null;
        }
    }

    @Subscribe("restApisTable")
    public void onRestApisTableSelection(Table.SelectionEvent<DtoRestApi> event) {
        if (restApisTable.getSingleSelected() == null) {
            return;
        }
        if (restApisTable.getSingleSelected().getImportError() != null) {
            errorLogField.setValue(restApisTable.getSingleSelected().getImportError());
        }
        else {
            errorLogField.setValue("");
        }
    }


    private int importSingleApi(DtoRestApi dtoRestApi, StringBuilder errorMsg) {
        int intRs = 0;
        String err_msg = "";

        try {
            if (dtoRestApi.getName() == null) {
                throw new Exception("Name of API is NULL");
            }

            if (Utility.findSpecialChar(dtoRestApi.getName()) != 0) {
                throw new Exception("Name must not contain special characters");
            }

            String basePath = dtoRestApi.getBase_path();
            String tmpBasePath = basePath.substring(1, basePath.length());

            if (basePath == null) {
                throw new Exception("Base path is NULL");
            }

            if (Utility.findSpecialChar(tmpBasePath) != 0) {
                throw new Exception("Base path must not contain special characters");
            }

            if (!basePath.substring(0, 1).equals("/")) {
                throw new Exception("Base path not valid");
            }

            if (!Utility.validateUrl(dtoRestApi.getTarget_endpoint())) {
                throw new Exception("Target endpoint is not valid URL");
            }

            RestApi restApi = dataManager.create(RestApi.class);
            restApi.setBase_path(dtoRestApi.getBase_path());
            restApi.setTarget_endpoint(dtoRestApi.getTarget_endpoint());
            restApi.setTitle(dtoRestApi.getTitle());
            restApi.setName(dtoRestApi.getName());

            String securityMethod = dtoRestApi.getSecurity_method();
            ApiSecurityMethod apiSecurityMethod = null;
            if (securityMethod == null) {
                err_msg = "Security method NULL";
                throw new NullPointerException(err_msg);
            }
            if (securityMethod.equals("Basic")) {
                apiSecurityMethod = basicSecurityMethod;
            }
            else {
                if (securityMethod.equals("API key")) {
                    apiSecurityMethod = apiKeySecurityMethod;
                }
                else {
                    if (securityMethod.equals("OAuth")) {
                        apiSecurityMethod = oauth2SecurityMethod;
                    }
                }

            }
            if (apiSecurityMethod == null) {
                err_msg = "Security method not found";
                throw new EntityNotFoundException(err_msg);
            }
            else {
                restApi.setSecurity_method(apiSecurityMethod);
            }

            User currUser = (User) currentAuthentication.getUser();
            restApi.setOwner(currUser);
            restApi.setOwner_apiname(currUser.getUsername() + "_" + restApi.getName());
            restApi.setPath_in_gw(currUser.getUsername().toLowerCase() + "/" + dtoRestApi.getBase_path().toLowerCase());
            dataManager.save(restApi);
        }
        catch (UniqueConstraintViolationException uniqueConstraintViolationException) {
            errorMsg.setLength(0);
            errorMsg.append("The API with that name already exists");
            intRs = -1;
        }
        catch (Exception e) {
            errorMsg.setLength(0);
            if (e.getMessage().isEmpty()) {
                errorMsg.append("Unknown error");
            }
            else {
                errorMsg.append(e.getMessage());
            }
            intRs = -1;
        }
        return intRs;
    }
    private DtoRestApi jsonObjectToApi(JsonObject jsonObject, StringBuilder err) {
        if (jsonObject == null) {
            return null;
        }
        DtoRestApi restApi = null;

        String attributeName = "";
        try {
            restApi = new DtoRestApi();
            attributeName = "name";
            restApi.setName(jsonObject.get("name").getAsString());
            attributeName = "base_path";
            restApi.setBase_path(jsonObject.get("base_path").getAsString());
            attributeName = "target_endpoint";
            restApi.setTarget_endpoint(jsonObject.get("target_endpoint").getAsString());
            attributeName = "security_method";
            restApi.setSecurity_method(jsonObject.get("security_method").getAsString());

        } catch (Exception e) {
            err.setLength(0);
            err.append("Failed when parse attribute of " + attributeName + ": "  + e.getMessage());
            restApi = null;
        }
        if (restApi == null) {
            return restApi;
        }
        try {
            restApi.setTitle(jsonObject.get("title").getAsString());
        }
        catch (Exception e) {
            err.setLength(0);
            err.append(e.getMessage());
        }
        return restApi;
    }
}