package com.company.apimanager.screen.jwtclient;

import com.company.apimanager.app.KongConnector;
import com.company.apimanager.app.KongOAuthClient;
import com.company.apimanager.app.kong_object.ConsumerJwtCredential;
import com.company.apimanager.entity.Consumer;
import com.company.apimanager.entity.JwtAlgorithm;
import com.company.apimanager.screen.publishedproduct.HandledError;
import io.jmix.core.DataManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.JwtClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@UiController("JwtClient.edit")
@UiDescriptor("jwt-client-edit.xml")
@EditedEntityContainer("jwtClientDc")
public class JwtClientEdit extends StandardEditor<JwtClient> {
    @Autowired
    private Notifications notifications;
    private Consumer consumer;
    @Autowired
    private ComboBox<String> algorithmComboBox;
    @Autowired
    private TextField<String> algorithmField;
    @Autowired
    private DataManager dataManager;

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
//        algorithmField.setValue("HS256");
//        algorithmField.setEditable(false);
        List<String> list = new ArrayList<>();
        for (JwtAlgorithm env : JwtAlgorithm.values()) {
            list.add(env.name());
        }
        algorithmComboBox.setTextInputAllowed(false);
        algorithmComboBox.setOptionsList(list);
        algorithmComboBox.setValue(algorithmField.getValue());
    }

    @Subscribe("algorithmComboBox")
    public void onAlgorithmComboBoxValueChange(HasValue.ValueChangeEvent event) {
        algorithmField.setValue(algorithmComboBox.getValue());
    }


    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        if ((getEditedEntity().getSecret() == null) || (getEditedEntity().getAlgorithm() == null)) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption("Please enter input values").show();
            event.preventCommit();
            return;
        }

        if ((getEditedEntity().getSecret().isEmpty()) ||
                (getEditedEntity().getAlgorithm().isEmpty())) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption("Please enter input values").show();
            event.preventCommit();
            return;
        }

        KongConnector kongConnector = new KongConnector(consumer.getSite().getGateway().getManagement_endpoint());
        HandledError handledError = new HandledError();

        ConsumerJwtCredential kongConsumerJwtCredential = new ConsumerJwtCredential();
        kongConsumerJwtCredential.setKey(getEditedEntity().getKey());
        kongConsumerJwtCredential.setAlgorithm(getEditedEntity().getAlgorithm());
        kongConsumerJwtCredential.setRsa_public_key(getEditedEntity().getRsa_public_key());
        kongConsumerJwtCredential.setSecret(getEditedEntity().getSecret());
        StringBuilder idCredential = new StringBuilder();
        int callToKong = kongConnector.createJwtClientForConsumer(consumer.getProvider_site_consumer_name()
                , kongConsumerJwtCredential, idCredential, handledError);

        if (callToKong == -1) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(handledError.getErrorMsg()).show();
            event.preventCommit();
            return;
        }
        getEditedEntity().setId(idCredential.toString());
        consumer.setJwtInfo(getEditedEntity());
        dataManager.save(consumer);


    }
}