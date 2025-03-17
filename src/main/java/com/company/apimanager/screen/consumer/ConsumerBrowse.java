package com.company.apimanager.screen.consumer;

import com.company.apimanager.app.KongConnector;
import com.company.apimanager.app.KongServicePlugin;
import com.company.apimanager.entity.*;
import com.company.apimanager.screen.jwtclient.JwtClientEdit;
import com.company.apimanager.screen.oauthclient.OAuthClientEdit;
import com.company.apimanager.screen.productsubscription.ProductSubscriptionBrowse;
import com.company.apimanager.screen.publishedproduct.HandledError;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.Screens;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.List;

@UiController("Consumer.browse")
@UiDescriptor("consumer-browse.xml")
@LookupComponent("consumersTable")
public class ConsumerBrowse extends StandardLookup<Consumer> {
    @Autowired
    private CollectionLoader<Consumer> consumersDl;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Button subcriptionBtn;
    @Autowired
    private GroupTable<Consumer> consumersTable;
    @Autowired
    private Screens screens;

    private String providerPlan;
    @Autowired
    private Button createBtn;
    @Autowired
    private DataManager dataManager;
    @Named("consumersTable.remove")
    private RemoveAction<Consumer> consumersTableRemove;
    @Autowired
    private Button remove1Btn;
    @Autowired
    private Button oAuthBtn;
    @Autowired
    private Button jwtBtn;

    @Subscribe
    public void onInit(InitEvent event) {
        User user = (User) currentAuthentication.getUser();
        consumersDl.setParameter("user", user);
        subcriptionBtn.setEnabled(false);
        oAuthBtn.setEnabled(false);
        jwtBtn.setEnabled(false);
        remove1Btn.setEnabled(false);

        User currUser = (User) currentAuthentication.getUser();
        providerPlan = currUser.getProviderPlan();
        if (providerPlan == null) {
            providerPlan = "Pilot";
        }
    }

    @Subscribe("oAuthBtn")
    public void onOAuthBtnClick(Button.ClickEvent event) {
        /*
        Consumer selectedConsumer = consumersTable.getSingleSelected();
        KongConnector kongConnector = new KongConnector(selectedConsumer.getSite().getGateway().getManagement_endpoint());
        KongServicePlugin[] kongServicePlugins = kongConnector.getServicePlugins
                ("bidv.Public.customer", new HandledError());
        int a = 0;
        */

        Consumer selectedConsumer = consumersTable.getSingleSelected();
        if (selectedConsumer == null) {
            return;
        }
        if (selectedConsumer.getKong_oauth_id() != null) {
            if (!selectedConsumer.getOauth_client_id().isEmpty()) {
                notifications.create(Notifications.NotificationType.WARNING)
                        .withCaption("The consumer already has OAuth client").show();
                return;
            }
        }
        OAuthClientEdit oAuthClientEdit = screens.create(OAuthClientEdit.class);
        OAuthClient oAuthClient = new OAuthClient();
        oAuthClient.setConsumerName(selectedConsumer.getName());
        oAuthClientEdit.setEntityToEdit(oAuthClient);
        oAuthClientEdit.setConsumer(selectedConsumer);
        oAuthClientEdit.show();
    }

    @Subscribe("jwtBtn")
    public void onJwtBtnClick(Button.ClickEvent event) {
        Consumer selectedConsumer = consumersTable.getSingleSelected();
        if (selectedConsumer == null) {
            return;
        }
        JwtClientEdit jwtClientEditScreem = screens.create(JwtClientEdit.class);
        JwtClient jwtClientObj = new JwtClient();
        jwtClientObj.setKey(selectedConsumer.getName());
        jwtClientObj.setAlgorithm(selectedConsumer.getJwtAlgorithm());
        jwtClientObj.setSecret(selectedConsumer.getJwtSecret());
        jwtClientObj.setRsa_public_key(selectedConsumer.getJwtRsaPublicKey());
        jwtClientEditScreem.setEntityToEdit(jwtClientObj);
        jwtClientEditScreem.setConsumer(selectedConsumer);
        jwtClientEditScreem.show();
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (providerPlan.equals("Pilot")) {
            if (consumersTable.getItems().size() > 0) {
                createBtn.setEnabled(false);
            }
            else {
                createBtn.setEnabled(true);
            }
        }
    }

    @Install(to = "consumersTable.create", subject = "afterCommitHandler")
    private void consumersTableCreateAfterCommitHandler(Consumer consumer) {
        if (providerPlan.equals("Pilot")) {
            if (consumersTable.getItems().size() > 0) {
                createBtn.setEnabled(false);
            }
            else {
                createBtn.setEnabled(true);
            }
        }
    }

    @Install(to = "consumersTable.remove", subject = "afterActionPerformedHandler")
    private void consumersTableRemoveAfterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent afterActionPerformedEvent) {
        HandledError handledError = new HandledError();
        try {
            Consumer consumer = (Consumer) afterActionPerformedEvent.getItems().get(0);
            KongConnector kongConnector = new KongConnector(consumer.getSite().getGateway().getManagement_endpoint());
            kongConnector.deleteConsumer(consumer.getKong_consumer_id(), handledError);

            if (providerPlan.equals("Pilot")) {
                if (consumersTable.getItems().size() > 0) {
                    createBtn.setEnabled(false);
                }
                else {
                    createBtn.setEnabled(true);
                }
            }
        }
        catch (Exception e) {
            notifications.create(Notifications.NotificationType.ERROR).withCaption(e.getMessage()).show();
        }
    }

    @Subscribe("consumersTable")
    public void onConsumersTableSelection(Table.SelectionEvent<Consumer> event) {
        subcriptionBtn.setEnabled(true);
        remove1Btn.setEnabled(true);
        //oAuthBtn.setEnabled(true);
        Consumer selectedConsumer = consumersTable.getSingleSelected();
        if (selectedConsumer == null) {
            jwtBtn.setEnabled(false);
            oAuthBtn.setEnabled(false);
            return;
        }
        if (selectedConsumer.getKong_oauth_id() != null) {
            jwtBtn.setEnabled(false);
            oAuthBtn.setEnabled(false);
        }
        else {
            jwtBtn.setEnabled(true);
            oAuthBtn.setEnabled(true);
        }
    }

    @Subscribe("subcriptionAction")
    public void onSubcriptionAction(Action.ActionPerformedEvent event) {
        Consumer selectedConsumer = consumersTable.getSingleSelected();
        if (selectedConsumer != null) {
            ProductSubscriptionBrowse productSubscriptionBrowse = screens.create(ProductSubscriptionBrowse.class);
            productSubscriptionBrowse.setConsumer(selectedConsumer);
            productSubscriptionBrowse.show();
        }
    }

    @Subscribe("remove1Btn")
    public void onRemove1BtnClick(Button.ClickEvent event) {
        Consumer selectedConsumer = consumersTable.getSingleSelected();
        if (selectedConsumer == null) {
            return;
        }
        List<ProductSubscription> productSubscriptions = dataManager.load(ProductSubscription.class).
                query("select e from ProductSubscription e where e.consumer = :consumer")
                .parameter("consumer", selectedConsumer).list();
        boolean noSub = false;
        if (productSubscriptions == null) {
            noSub = true;
        }
        else {
            if (productSubscriptions.size() == 0) {
                noSub = true;
            }
        }
        if (noSub) {
            consumersTableRemove.execute();
        }
        else {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Could not remove that consumer as there are still subscriptions of that consumer!").show();
        }
    }

}