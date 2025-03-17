package com.company.apimanager.screen.product;

import com.company.apimanager.entity.*;
import com.company.apimanager.screen.apiregister.ApiRegisterBrowse;
import com.company.apimanager.screen.apiregister.ApiRegisterEdit;
import com.company.apimanager.screen.brandlogin.ApiRegister;
import com.company.apimanager.screen.publishedproduct.HandledError;
import com.company.apimanager.screen.publishedproduct.PublishedProductBrowse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.jmix.core.DataManager;
import io.jmix.core.TimeSource;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.data.exception.UniqueConstraintViolationException;
import io.jmix.ui.Notifications;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.Screens;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.data.TableItems;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.inject.Named;
import java.util.Arrays;
import java.util.List;

@UiController("Product.browse")
@UiDescriptor("product-browse.xml")
@LookupComponent("productsTable")
public class ProductBrowse extends StandardLookup<Product> {
    @Autowired
    private CollectionLoader<Product> productsDl;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private Screens screens;
    @Autowired
    private GroupTable<Product> productsTable;
    @Autowired
    private Button registerBtn;
    @Autowired
    private Button stagingBtn;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private TimeSource timeSource;
    @Autowired
    private Notifications notifications;

    private String providerPlan;
    @Autowired
    private Button createBtn;
    @Named("productsTable.remove")
    private RemoveAction<Product> productsTableRemove;
    @Autowired
    private Button remove1Btn;

    @Subscribe
    public void onInit(InitEvent event) {
        User user = (User) currentAuthentication.getUser();
        productsDl.setParameter("user", user);
        registerBtn.setEnabled(false);
        stagingBtn.setEnabled(false);
        remove1Btn.setEnabled(false);

        providerPlan = user.getProviderPlan();
        if (providerPlan == null) {
            providerPlan = "Pilot";
        }
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (providerPlan.equals("Pilot")) {
            if (providerPlan.equals("Pilot")) {
                if (productsTable.getItems().size() > 0) {
                    createBtn.setEnabled(false);
                }
                else {
                    createBtn.setEnabled(true);
                }
            }
        }
    }

    @Install(to = "productsTable.create", subject = "afterCommitHandler")
    private void productsTableCreateAfterCommitHandler(Product product) {
        if (providerPlan.equals("Pilot")) {
            if (productsTable.getItems().size() > 0) {
                createBtn.setEnabled(false);
            }
            else {
                createBtn.setEnabled(true);
            }
        }
    }

    @Install(to = "productsTable.remove", subject = "afterActionPerformedHandler")
    private void productsTableRemoveAfterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent<Product> afterActionPerformedEvent) {
        if (providerPlan.equals("Pilot")) {
            if (productsTable.getItems().size() > 0) {
                createBtn.setEnabled(false);
            }
            else {
                createBtn.setEnabled(true);
            }
        }
        if (productsTable.getItems().size() == 0) {
            registerBtn.setEnabled(false);
            stagingBtn.setEnabled(false);
            remove1Btn.setEnabled(false);
        }
    }

    @Subscribe("registerAction")
    public void onRegisterAction(Action.ActionPerformedEvent event) {
        Product product = productsTable.getSingleSelected();
        if (product != null) {
            ApiRegisterBrowse apiRegisterBrowse = screens.create(ApiRegisterBrowse.class);
            apiRegisterBrowse.setProduct(product);
            apiRegisterBrowse.show();
        }
    }

    @Subscribe("stagingAction")
    public void onStagingAction(Action.ActionPerformedEvent event) {
        Product product = productsTable.getSingleSelected();
        if (product != null) {
            User currUser = (User) currentAuthentication.getUser();
            if (providerPlan.equals("Enterprise")) {
                PublishedProductBrowse publishedProductBrowse = screens.create(PublishedProductBrowse.class);
                publishedProductBrowse.setProduct(product);
                publishedProductBrowse.setProductState("STAGED");
                publishedProductBrowse.show();
            }
            else {
                HandledError handledError = new HandledError();
                if (this.stageToDefaultSite(product, handledError) != 0) {
                    notifications.create(Notifications.NotificationType.ERROR).
                            withCaption(handledError.getErrorMsg()).show();
                }
                else {
                    notifications.create(Notifications.NotificationType.HUMANIZED).
                            withCaption("Staging successful").show();
                }
            }
        }
    }
    private Site getDefaultSite(HandledError handledError) {
        Site rsSite = null;
        try {
            List<Site> siteList = dataManager.load(Site.class).query("select e from Site e where e.owner = :owner")
                    .parameter("owner", (User) currentAuthentication.getUser()).list();
            if (siteList == null) {
                rsSite = null;
                handledError.setErrorMsg("Could not get default site");
            }
            else {
                if (siteList.size() == 0) {
                    rsSite = null;
                    handledError.setErrorMsg("Could not get default site");
                }
                else {
                    rsSite = siteList.get(0);
                }
            }
        }
        catch (Exception e) {
            rsSite = null;
            handledError.setErrorMsg(e.getMessage());
        }
        return rsSite;
    }
    private int stageToDefaultSite(Product product, HandledError handledError) {
        int intRet = 0;

        Site defaultSite = this.getDefaultSite(handledError);
        if (defaultSite == null) {
            return -2;
        }
        try {
            PublishedProduct stagedProduct = dataManager.create(PublishedProduct.class);
            stagedProduct.setProduct(product);
            stagedProduct.setSite(defaultSite);
            stagedProduct.setState("STAGED");
            stagedProduct.setName(product.getName());
            stagedProduct.setPublished_date(timeSource.currentTimestamp());
            stagedProduct.setProduct_site_id(product.getId().toString() + "_" +
                    defaultSite.getId().toString());
            dataManager.save(stagedProduct);

            List<ApiRegister> apiRegisters = dataManager.load(ApiRegister.class)
                    .query("select a from ApiRegister a where a.product=:product")
                    .parameter("product", stagedProduct.getProduct())
                    .list();

            /*
            List<DrupalLink> drupalLinkList = dataManager.load(DrupalLink.class)
                    .query("select d from DrupalLink d where d.publishedProduct=:published_product")
                    .parameter("published_product", stagedProduct)
                    .list();

            if (!drupalLinkList.isEmpty()) {
                dataManager.remove(drupalLinkList);
            }

            if (!apiRegisters.isEmpty()) {
                for (ApiRegister apiRegister:apiRegisters) {
                    DrupalLink drupalLink = dataManager.create(DrupalLink.class);
                    drupalLink.setRestApi(apiRegister.getApi());
                    drupalLink.setPublishedProduct(stagedProduct);
                    dataManager.save(drupalLink);
                }
            }

             */
        }
        catch (UniqueConstraintViolationException uniqueException) {
            handledError.setErrorMsg("The product already in Public");
            intRet = -2;
        }
        catch (Exception e) {
            handledError.setErrorMsg(e.getMessage());
            intRet = -2;
        }
        return intRet;
    }

    @Subscribe("productsTable")
    public void onProductsTableSelection(Table.SelectionEvent<Product> event) {
        registerBtn.setEnabled(true);
        stagingBtn.setEnabled(true);
        remove1Btn.setEnabled(true);
    }

    @Subscribe("remove1Btn")
    public void onRemove1BtnClick(Button.ClickEvent event) {
        Product selectedProduct = productsTable.getSingleSelected();
        if (selectedProduct == null) {
            return;
        }
        List<PublishedProduct> publishedProducts = dataManager.load(PublishedProduct.class).
                query("select e from PublishedProduct e where e.product = :product").
                parameter("product", selectedProduct).list();
        boolean noProducts = false;
        if (publishedProducts == null) {
            noProducts = true;
        }
        else {
            if (publishedProducts.size() == 0) {
                noProducts = true;
            }
        }
        if (!noProducts) {
            String siteNames = "";
            for (int i = 0; i < publishedProducts.size(); i++) {
                siteNames = siteNames + publishedProducts.get(i).getSite().getName() + " ";
            }
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Could not remove that product as the product still on sites: "
                    + siteNames).show();
            return;
        }

        List<ApiRegister> apiRegisters =  dataManager.load(ApiRegister.class).
                query("select e from ApiRegister e where e.product = :product")
                .parameter("product", selectedProduct).list();
        noProducts = false;
        if (apiRegisters == null) {
            noProducts = true;
        }
        else {
            if (apiRegisters.size() == 0) {
                noProducts = true;
            }
        }
        if (noProducts) {
            productsTableRemove.execute();
        }
        else {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Could not remove that product as there are still API in the product!").show();
        }
    }
}