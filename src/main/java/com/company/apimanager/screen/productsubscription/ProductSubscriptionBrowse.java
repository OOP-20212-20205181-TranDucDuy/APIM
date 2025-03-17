package com.company.apimanager.screen.productsubscription;

import com.company.apimanager.app.KongConnector;
import com.company.apimanager.entity.Consumer;
import com.company.apimanager.entity.Site;
import com.company.apimanager.entity.User;
import com.company.apimanager.screen.publishedproduct.HandledError;
import groovyjarjarantlr4.v4.tool.GrammarParserInterpreter;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.ProductSubscription;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;

@UiController("ProductSubscription.browse")
@UiDescriptor("product-subscription-browse.xml")
@LookupComponent("productSubscriptionsTable")
public class ProductSubscriptionBrowse extends StandardLookup<ProductSubscription> {
    private Consumer consumer;
    @Autowired
    private CollectionLoader<ProductSubscription> productSubscriptionsDl;
    @Autowired
    private GroupTable<ProductSubscription> productSubscriptionsTable;
    @Named("productSubscriptionsTable.remove")
    private RemoveAction<ProductSubscription> productSubscriptionsTableRemove;
    @Autowired
    private Notifications notifications;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    private Button unSubcribeBtn;
    @Autowired
    private Label subscriptionLabel;

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        unSubcribeBtn.setEnabled(false);
    }

    @Subscribe("productSubscriptionsTable")
    public void onProductSubscriptionsTableSelection(Table.SelectionEvent<ProductSubscription> event) {
        if (productSubscriptionsTable.getSingleSelected() != null) {
            unSubcribeBtn.setEnabled(true);
        }
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        String value = "<b><font size=\"3\">"
                + "Products subscribed by " + this.consumer.getName()
                + "</font><b/>";
        subscriptionLabel.setHtmlEnabled(true);
        subscriptionLabel.setHtmlSanitizerEnabled(true);
        subscriptionLabel.setValue(value);
    }


    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
        productSubscriptionsDl.setParameter("consumer", consumer);
    }

    @Install(to = "productSubscriptionsTable.create", subject = "initializer")
    private void productSubscriptionsTableCreateInitializer(ProductSubscription productSubscription) {
        productSubscription.setConsumer(consumer);
    }

    @Subscribe("unSubcribeBtn")
    public void onUnSubcribeBtnClick(Button.ClickEvent event) {
        dialogs.createOptionDialog()
                .withCaption("Confirmation")
                .withMessage("Are you sure unsubcriber consumer?")
                .withActions(
                        new DialogAction(DialogAction.Type.OK)
                                .withHandler(e ->
                                        this.handleUnsubConsumer()
                                ),
                        new DialogAction(DialogAction.Type.CANCEL))
                .show();

//        handleUnsubConsumer();
    }

    public void handleUnsubConsumer() {
        try {
            ProductSubscription productSubscription = productSubscriptionsTable.getSingleSelected();
            Site site = productSubscription.getPublished_product().getSite();
            //KongConnector kongConnector = new KongConnector(site.getGateway().getManagement_endpoint());
            KongConnector kongConnector = new KongConnector(site.getDefaultManagementEndpoint());
            String kongConsumerName = consumer.getProvider().getUsername().toLowerCase()
                    + "." + site.getName().toLowerCase()
                    + "." + consumer.getName().toLowerCase();
            HandledError handledError = new HandledError();
            if (kongConnector.unJoinConsumerFromGroup(kongConsumerName, productSubscription.getKong_consumer_group_id(),
                    handledError) == 204) {
                productSubscriptionsTableRemove.execute();
            } else {
                notifications.create(Notifications.NotificationType.ERROR).withCaption(handledError.getErrorMsg()).show();
            }
        }
        catch (Exception e) {
            notifications.create(Notifications.NotificationType.ERROR).withCaption(e.getMessage()).show();
        }
    }

}