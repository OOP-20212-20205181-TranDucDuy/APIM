package com.company.apimanager.screen.oauthclient;

import com.company.apimanager.app.KongConnector;
import com.company.apimanager.app.KongOAuthClient;
import com.company.apimanager.entity.Consumer;
import com.company.apimanager.entity.Site;
import com.company.apimanager.screen.publishedproduct.HandledError;
import io.jmix.core.DataManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.OAuthClient;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("OAuthClient.edit")
@UiDescriptor("o-auth-client-edit.xml")
@EditedEntityContainer("oAuthClientDc")
public class OAuthClientEdit extends StandardEditor<OAuthClient> {
    @Autowired
    private Notifications notifications;
    private Consumer consumer;
    @Autowired
    private DataManager dataManager;

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        if ((getEditedEntity().getClientName() == null) || (getEditedEntity().getClientRedirectUri() == null)) {
            notifications.create(Notifications.NotificationType.WARNING)
                            .withCaption("Please enter input values").show();
            event.preventCommit();
        }

        if ((getEditedEntity().getClientName().isEmpty()) ||
                (getEditedEntity().getClientRedirectUri().isEmpty())) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption("Please enter input values").show();
            event.preventCommit();
            return;
        }
        KongConnector kongConnector = new KongConnector(consumer.getSite().getGateway().getManagement_endpoint());
        HandledError handledError = new HandledError();
        KongOAuthClient kongOAuthClient= kongConnector.createOAuthClientForConsumer(consumer.getProvider_site_consumer_name()
                            , getEditedEntity().getClientName(), getEditedEntity().getClientRedirectUri(), handledError);
        if (kongOAuthClient == null) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(handledError.getErrorMsg()).show();
            event.preventCommit();
            return;
        }
        consumer.setOAuthInfo(kongOAuthClient);
        dataManager.save(consumer);
    }
}