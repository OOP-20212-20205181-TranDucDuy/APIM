package com.company.apimanager.screen.selfhostedgateway;

import com.company.apimanager.app.Utility;
import com.company.apimanager.entity.AppConfiguration;
import com.company.apimanager.entity.GatewayType;
import com.company.apimanager.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.SelfHostedGateway;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@UiController("SelfHostedGateway.edit")
@UiDescriptor("self-hosted-gateway-edit.xml")
@EditedEntityContainer("selfHostedGatewayDc")
public class SelfHostedGatewayEdit extends StandardEditor<SelfHostedGateway> {
    @Autowired
    private Notifications notifications;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Utility utility;

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        String name = getEditedEntity().getName();
        if (Utility.findSpecialChar(name) != 0) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Name must not contain special characters")
                    .show();
            event.preventCommit();
            return;
        }
        if (!Utility.validateUrl(getEditedEntity().getManagement_endpoint())) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Management endpoint is not valid URL")
                    .show();
            event.preventCommit();
            return;
        }
        User currUser = (User) currentAuthentication.getUser();
        getEditedEntity().setOwner(currUser);
        getEditedEntity().setOwner_gatewayname(currUser.getUsername() + "_" + name);
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        //docker ps

        /*
        String cmd = utility.getAppConfigValue("newGatewayCustomerDomainDockerCmd");
        if (cmd == null) {
            notifications.create(Notifications.NotificationType.ERROR).
                    withCaption("Could not get configuration of customer domain").show();
            return;
        }

        StringBuilder output = new StringBuilder();
        StringBuilder err = new StringBuilder();
        if (Utility.executeOsCommand(cmd, output, err) == 0) {
            notifications.create(Notifications.NotificationType.HUMANIZED).
                    withCaption(output.toString()).show();
        }
        else {
            notifications.create(Notifications.NotificationType.ERROR).
                    withCaption("Error: " + err).show();
        }
         */
    }
}