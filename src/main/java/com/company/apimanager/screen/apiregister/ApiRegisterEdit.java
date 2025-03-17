package com.company.apimanager.screen.apiregister;

import com.company.apimanager.entity.DrupalLink;
import com.company.apimanager.entity.Product;
import com.company.apimanager.entity.PublishedProduct;
import com.company.apimanager.entity.RestApi;
import io.jmix.core.DataManager;
import io.jmix.core.TimeSource;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.DateField;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.Label;
import io.jmix.ui.screen.*;
import com.company.apimanager.screen.brandlogin.ApiRegister;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@UiController("ApiRegister.edit")
@UiDescriptor("api-register-edit.xml")
@EditedEntityContainer("apiRegisterDc")
public class ApiRegisterEdit extends StandardEditor<ApiRegister> {
    @Autowired
    private DateField<LocalDateTime> register_datetimeField;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private TimeSource timeSource;
    @Autowired
    private Label editorLabel;
    private Product product;
    @Autowired
    private EntityPicker<RestApi> apiField;

    private List<RestApi> apiList;
    @Autowired
    private Notifications notifications;

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setApiList(List<RestApi> apiList) {
        this.apiList = apiList;
    }

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        getEditedEntity().setRegister_datetime(timeSource.currentTimestamp());
        //getEditedEntity().setProduct(product);
        getEditedEntity().setApi_product_id(getEditedEntity().getApi().getId().toString() + "_"+
                getEditedEntity().getProduct().getId().toString());
        String newBasePath = getEditedEntity().getApi().getBase_path();
        List<ApiRegister> apiRegisters = dataManager.load(ApiRegister.class)
                .query("select a from ApiRegister a where a.product = :product")
                .parameter("product", getEditedEntity().getProduct())
                .list();

        boolean basePathDuplicate = false;
        String apiName = "";
        if (apiRegisters != null) {
            for (int i = 0; i < apiRegisters.size(); i++) {
                if (newBasePath.equals(apiRegisters.get(i).getApi().getBase_path())) {
                    basePathDuplicate = true;
                    apiName = apiRegisters.get(i).getApi().getName();
                    break;
                }
            }
            if (basePathDuplicate) {
                notifications.create(Notifications.NotificationType.ERROR).
                        withCaption("The base path already registered in the Product (the API with name of " + apiName
                        + ")").show();
                event.preventCommit();
                return;
            }
        }
    }

    @Subscribe
    public void onAfterCommitChanges(AfterCommitChangesEvent event) {
        /*
        Product product = getEditedEntity().getProduct();
        List<PublishedProduct> publishedProducts = dataManager.load(PublishedProduct.class)
                .query("select p from PublishedProduct p where p.product=:product")
                .parameter("product", product)
                .list();

        Map<UUID, UUID> drupalLinks = dataManager.load(DrupalLink.class)
                .condition(
                        LogicalCondition.and(
                            PropertyCondition.inList("publishedProduct", publishedProducts),
                            PropertyCondition.equal("restApi", getEditedEntity().getApi()))
                )
                .list()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> (tuple.getPublishedProduct().getId()),
                        tuple -> (tuple.getPublishedProduct().getId())
                ));

        for (PublishedProduct publishedProduct:publishedProducts) {
            if (publishedProduct.getId() == drupalLinks.get(publishedProduct.getId())) {
                continue;
            }

            DrupalLink drupalLink = dataManager.create(DrupalLink.class);
            drupalLink.setRestApi(getEditedEntity().getApi());
            drupalLink.setPublishedProduct(publishedProduct);
            dataManager.save(drupalLink);
        }

         */
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        String value = "<b><font size=\"3\">"
                + "Select API to register to "
                + getEditedEntity().getProduct().getName()
                + "</font><b/>";
        editorLabel.setHtmlEnabled(true);
        editorLabel.setHtmlSanitizerEnabled(true);
        editorLabel.setValue(value);
    }
}