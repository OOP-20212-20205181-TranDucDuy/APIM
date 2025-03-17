package com.company.apimanager.screen.apiinsite;

import com.company.apimanager.app.Utility;
import com.company.apimanager.entity.Consumer;
import com.company.apimanager.entity.RestApi;
import com.company.apimanager.entity.Site;
import com.company.apimanager.screen.restapi.RestApiEdit;
import io.jmix.core.DataManager;
import io.jmix.ui.Actions;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.TextField;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.ApiInSite;
import org.springframework.beans.factory.annotation.Autowired;
import io.jmix.ui.Screens;

import java.util.UUID;

@UiController("ApiInSite.edit")
@UiDescriptor("api-in-site-edit.xml")
@EditedEntityContainer("apiInSiteDc")
public class ApiInSiteEdit extends StandardEditor<ApiInSite> {
    @Autowired
    private TextField<String> consent_urlField;
    @Autowired
    private BackgroundWorker backgroundWorker;
    @Autowired
    private Actions actions;

    @Autowired
    private Screens screens;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DataManager dataManager;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (consent_urlField.getValue() == null) {
            return;
        }
        if (consent_urlField.getValue().equals("")) {
            return;
        }
        String provisionKey = getEditedEntity().getOauthProvisionKey();
        if (!Utility.CheckNullAndEmpty(provisionKey)) {
            consent_urlField.setContextHelpText
                    ("To get authorization code invoke Consent URL with client=Consumer OAuth client_id and redirect=consumer redicrect URL: \n"
                            + consent_urlField.getValue() + "&client=client_id&redirect=redirect_url");
        }
    }

    @Subscribe("goToRestApiBtn")
    public void onGoToRestApiBtnClick(Button.ClickEvent event) {
        try {
            UUID restApiId = UUID.fromString(getEditedEntity().getRestApiId());
            RestApiEdit restApiEdit = screens.create(RestApiEdit.class);
            RestApi restApi = dataManager.load(RestApi.class).id(restApiId).one();
            restApiEdit.setEntityToEdit(restApi);
            restApiEdit.show();
        }
        catch (Exception e) {
            String msg = e.getMessage();
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption(msg)
                    .show();
        }
    }
}