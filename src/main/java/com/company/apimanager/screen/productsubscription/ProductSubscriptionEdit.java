package com.company.apimanager.screen.productsubscription;

import com.company.apimanager.app.KongConnector;
import com.company.apimanager.app.KongConsumerGroupDetails;
import com.company.apimanager.entity.Consumer;
import com.company.apimanager.entity.PublishedProduct;
import com.company.apimanager.entity.Site;
import com.company.apimanager.screen.publishedproduct.HandledError;
import io.jmix.core.TimeSource;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.ProductSubscription;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("ProductSubscription.edit")
@UiDescriptor("product-subscription-edit.xml")
@EditedEntityContainer("productSubscriptionDc")
public class ProductSubscriptionEdit extends StandardEditor<ProductSubscription> {
    @Autowired
    private TimeSource timeSource;
    @Autowired
    private EntityComboBox<PublishedProduct> publishedProductCombobox;
    @Autowired
    private Notifications notifications;
    @Autowired
    private CollectionLoader<PublishedProduct> publishedProductsDl;

    @Subscribe
    public void onInitEntity(InitEntityEvent<ProductSubscription> event) {
        publishedProductsDl.setParameter("site", getEditedEntity().getConsumer().getSite());
        publishedProductsDl.setParameter("state", "PUBLISHED");
    }

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        try {
            getEditedEntity().setSubcription_date(timeSource.currentTimestamp());
            if (publishedProductCombobox.getValue() == null) {
             notifications.create(Notifications.NotificationType.WARNING).
                     withCaption("Please select product").show();
                event.preventCommit();
            }
            else {
                HandledError handledError = new HandledError();
                KongConsumerGroupDetails kongConsumerGroupDetails = new KongConsumerGroupDetails();
                int intJoinGroup = joinGroup(handledError, kongConsumerGroupDetails);
                if ((intJoinGroup != 201) && (intJoinGroup != 200)) {
                    notifications.create(Notifications.NotificationType.ERROR).
                            withCaption(handledError.getErrorMsg()).show();
                    event.preventCommit();
                }
                else {
                    getEditedEntity().setPublished_product(publishedProductCombobox.getValue());
                    getEditedEntity().setProduct_consumer_id(getEditedEntity().getConsumer().getId().toString()
                            + "." + getEditedEntity().getPublished_product().getId().toString());
                    getEditedEntity().setKong_consumer_group_id(kongConsumerGroupDetails.getId());
                }
            }
        }
        catch (Exception e) {
            notifications.create(Notifications.NotificationType.ERROR).
                    withCaption(e.getMessage()).show();
            event.preventCommit();
        }
    }

    private int joinGroup(HandledError handledError, KongConsumerGroupDetails kongConsumerGroupDetails) {
        Site site = publishedProductCombobox.getValue().getSite();
        KongConnector kongConnector = new KongConnector(site.getDefaultManagementEndpoint());
        Consumer consumer = getEditedEntity().getConsumer();
        String kongConsumerName = consumer.getProvider().getUsername().toLowerCase()
                + "." + site.getName().toLowerCase()
                + "." + consumer.getName().toLowerCase();
        String kongGroupName = consumer.getProvider().getUsername() + "."
                + site.getName() + "." + publishedProductCombobox.getValue().getProduct().getName();
        int intRet = kongConnector.joinConsumerToGroup(kongConsumerName, kongGroupName,
                kongConsumerGroupDetails, handledError);
        if (intRet == 201) {
            return intRet;
        }
        if (intRet != 409) {
            return intRet;
        }
        int intGetRet = kongConnector.getConsumerGroupInfo(kongConsumerName, kongGroupName,
                kongConsumerGroupDetails, handledError);
        return intGetRet;
    }

}