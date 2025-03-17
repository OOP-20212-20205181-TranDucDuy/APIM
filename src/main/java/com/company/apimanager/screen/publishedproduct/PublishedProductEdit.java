package com.company.apimanager.screen.publishedproduct;

import com.company.apimanager.entity.*;
import com.company.apimanager.screen.brandlogin.ApiRegister;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.jmix.core.DataManager;
import io.jmix.core.TimeSource;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.component.Label;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@UiController("PublishedProduct.edit")
@UiDescriptor("published-product-edit.xml")
@EditedEntityContainer("publishedProductDc")
public class PublishedProductEdit extends StandardEditor<PublishedProduct> {

    @Autowired
    private TimeSource timeSource;
    @Autowired
    private Label editorLabel;
    @Autowired
    private DataManager dataManager;
    @Autowired
    Notifications notifications;
    private Exception e;
    @Autowired
    private EntityComboBox<Site> siteComboBox;
    private List<String> pulishedSites;
    @Autowired
    private CollectionLoader<Site> sitesDl;
    @Autowired
    private CurrentAuthentication currentAuthentication;

    private final Logger log = LoggerFactory.getLogger(PublishedProductEdit.class);
    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        //Create service, route in Kong gateway

        Site site = siteComboBox.getValue();
        if (site == null) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Please select site to publish!")
                    .show();
            event.preventCommit();
            return;
        }
        //Check if product already in the site
        boolean alreadyBool = false;
        if (pulishedSites != null) {
            for (int i = 0; i < pulishedSites.size(); i++) {
                if (pulishedSites.get(i).equals(site.getName())) {
                    alreadyBool = true;
                    break;
                }
            }
        }
        if (alreadyBool) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Product already staged in that site!")
                    .show();
            event.preventCommit();
            return;
        }

        //Get APIs already on the site
        List<PublishedProduct> publishedProductList = dataManager.load(PublishedProduct.class).query("select e from PublishedProduct e" +
                " where e.site = :site").parameter("site", site).list();
        if (publishedProductList != null) {
            if (publishedProductList.size() >= 0) {
                List<Product> products = new ArrayList<Product>(publishedProductList.size());
                for (int i = 0; i < publishedProductList.size(); i++) {
                    products.add(publishedProductList.get(i).getProduct());
                }

                List<ApiRegister> apiRegisters = dataManager.load(ApiRegister.class)
                        .query("select a from ApiRegister a where a.product in :product_list")
                        .parameter("product_list", products)
                        .list();
                if (apiRegisters != null) {
                    if (apiRegisters.size() != 0) {
                        Product theProduct = getEditedEntity().getProduct();

                        List<ApiRegister> newApiRegisters = dataManager.load(ApiRegister.class)
                                .query("select a from ApiRegister a where a.product = :product")
                                .parameter("product", theProduct)
                                .list();
                        if (newApiRegisters != null) {
                            for (int i = 0; i < newApiRegisters.size(); i++) {
                                for (int j = 0; j < apiRegisters.size(); j++) {
                                    if (newApiRegisters.get(i).getApi().getBase_path().equals(apiRegisters.get(j).getApi().getBase_path())) {
                                        String errMsg = "Could not stage the API with name \"" + newApiRegisters.get(i).getApi().getName()
                                                + "\" as Base path of that API is same to the API of " + apiRegisters.get(j).getApi().getName();
                                        notifications.create(Notifications.NotificationType.ERROR).
                                                withCaption(errMsg).show();
                                        event.preventCommit();
                                        return;
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        getEditedEntity().setSite(site);
        getEditedEntity().setPublished_date(timeSource.currentTimestamp());
        getEditedEntity().setProduct_site_id(getEditedEntity().getProduct().getId().toString()
                + "_" +site.getId().toString());
        //getEditedEntity().setKong_object_ids("kongObjIds");
        getEditedEntity().setName(getEditedEntity().getProduct().getName());
        getEditedEntity().setState("STAGED");

        if (getEditedEntity().getProduct().getVisibility_type().equals("Closed")){
            return;
        }
    }

    @Subscribe
    public void onAfterCommitChanges(AfterCommitChangesEvent event) {
        /*
        List<ApiRegister> apiRegisters = dataManager.load(ApiRegister.class)
                .query("select a from ApiRegister a where a.product=:product")
                .parameter("product", getEditedEntity().getProduct())
                .list();
        if (!apiRegisters.isEmpty()) {
            for (ApiRegister apiRegister:apiRegisters) {
                DrupalLink drupalLink = dataManager.create(DrupalLink.class);
                drupalLink.setRestApi(apiRegister.getApi());
                drupalLink.setPublishedProduct(getEditedEntity());
                dataManager.save(drupalLink);

            }
        }

         */
    }

    @Subscribe
    public void onInit(InitEvent event) {
        User user = (User) currentAuthentication.getUser();
        sitesDl.setParameter("owner", user);
    }

    @Subscribe
    public void onInitEntity(InitEntityEvent<PublishedProduct> event) {
        Product thatProduct = getEditedEntity().getProduct();
        List<PublishedProduct> publishedProductList = dataManager.load(PublishedProduct.class).query("select e from PublishedProduct e" +
                " where e.product = :product").parameter("product", thatProduct).list();
        if (publishedProductList != null) {
            if (publishedProductList.size() >= 0) {
                List<Site> siteList = new ArrayList<Site>(publishedProductList.size());
                for (int i = 0; i < publishedProductList.size(); i++) {
                    siteList.add(publishedProductList.get(i).getSite());
                }
                sitesDl.setParameter("sitelist", siteList);
            }
        }
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        String value = "<b><font size=\"3\">"
                + "Select Site to stage the product "
                + getEditedEntity().getProduct().getName()
                + "</font><b/>";
        editorLabel.setHtmlEnabled(true);
        editorLabel.setHtmlSanitizerEnabled(true);
        editorLabel.setValue(value);
    }
}