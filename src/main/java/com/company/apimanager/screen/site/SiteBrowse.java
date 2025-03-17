package com.company.apimanager.screen.site;

import com.company.apimanager.entity.Consumer;
import com.company.apimanager.entity.PublishedProduct;
import com.company.apimanager.entity.User;
import com.company.apimanager.screen.publishedproduct.PublishedProductBrowse;
import com.company.apimanager.screen.sitegateway.SiteGatewayBrowse;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.Screens;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.Site;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@UiController("Site.browse")
@UiDescriptor("site-browse.xml")
@LookupComponent("sitesTable")
public class SiteBrowse extends StandardLookup<Site> {
    @Autowired
    private CollectionLoader<Site> sitesDl;

    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private Button selectBtn;
    @Autowired
    private Button createBtn;
    @Autowired
    private Button editBtn;
    @Autowired
    private Button removeBtn;
    @Autowired
    private Button gatewaysBtn;
    @Autowired
    private Button productBtn;
    @Autowired
    private HBoxLayout lookupActions;
    @Autowired
    private GroupTable<Site> sitesTable;
    @Autowired
    private Screens screens;

    private User siteOwner;
    @Named("sitesTable.remove")
    private RemoveAction<Site> sitesTableRemove;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Button remove1Btn;
    @Autowired
    private MessageBundle messageBundle;

    public void setSiteOwner(User siteOwner) {
        this.siteOwner = siteOwner;
        sitesDl.setParameter("owner", siteOwner);
    }

    @Subscribe
    public void onInit(InitEvent event) throws IOException {
        sitesDl.setParameter("owner", (User) currentAuthentication.getUser());
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (lookupActions.isVisible()) {
            createBtn.setVisible(false);
            editBtn.setVisible(false);
            removeBtn.setVisible(false);
            productBtn.setVisible(false);
            gatewaysBtn.setVisible(false);
            remove1Btn.setVisible(false);
        }
        else {
            if (siteOwner != null) { //Thuc hien o giao dien Topo Admin, add site cho Provider
                productBtn.setVisible(false);
                gatewaysBtn.setVisible(false);
                editBtn.setVisible(false);

                //Nếu có site ròi, thì không cho phép tạo nữa
                if (sitesTable.getItems().size() > 0) {
                    createBtn.setEnabled(false);
                }
                else {
                    createBtn.setEnabled(true);
                }
                remove1Btn.setEnabled(false);
            }
            else {
                productBtn.setEnabled(false);
                gatewaysBtn.setEnabled(false);
                remove1Btn.setEnabled(false);
            }

        }
    }

    @Subscribe("sitesTable")
    public void onSitesTableSelection(Table.SelectionEvent<Site> event) {
        Site site = sitesTable.getSingleSelected();
        if (site == null) {
            return;
        }
        productBtn.setEnabled(true);
        gatewaysBtn.setEnabled(true);
        remove1Btn.setEnabled(true);
    }

    @Subscribe("productBtn")
    public void onProductBtnClick(Button.ClickEvent event) {
        Site site = sitesTable.getSingleSelected();
        if (site == null) {
            return;
        }
        PublishedProductBrowse publishedProductBrowse = screens.create(PublishedProductBrowse.class);
        publishedProductBrowse.setSite(site);
        publishedProductBrowse.setArchivedState("ARCHIVED");
        publishedProductBrowse.show();
    }

    @Subscribe("gatewaysBtn")
    public void onGatewaysBtnClick(Button.ClickEvent event) {
        Site site = sitesTable.getSingleSelected();
        if (site == null) {
            return;
        }
        SiteGatewayBrowse siteGatewayBrowse = screens.create(SiteGatewayBrowse.class);
        siteGatewayBrowse.setSite(site);
        siteGatewayBrowse.show();
    }

    @Install(to = "sitesTable.create", subject = "initializer")
    private void sitesTableCreateInitializer(Site site) {
        site.setOwner(siteOwner);
    } //Truyen siteOwner cho SiteEdit (not Null neu goi tu Topo Admin)

    @Install(to = "sitesTable.remove", subject = "afterActionPerformedHandler")
    private void sitesTableRemoveAfterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent<Site> afterActionPerformedEvent) {
        if (siteOwner != null) {
            if (sitesTable.getItems().size() == 0) {
                createBtn.setEnabled(true);
                remove1Btn.setEnabled(false);
            }
            else  {
                createBtn.setEnabled(false);
            }
        }
        else {
            if (sitesTable.getItems().size() == 0) {
                remove1Btn.setEnabled(false);
                productBtn.setEnabled(false);
            }
        }
    }

    @Install(to = "sitesTable.create", subject = "afterCommitHandler")
    private void sitesTableCreateAfterCommitHandler(Site site) {
        if (siteOwner != null) {
            if (sitesTable.getItems().size() == 0) {
                createBtn.setEnabled(true);
            } else {
                createBtn.setEnabled(false);
            }
        }
    }

    @Subscribe("remove1Btn")
    public void onRemove1BtnClick(Button.ClickEvent event) {
        Site selectedSite = sitesTable.getSingleSelected();
        if (selectedSite == null) {
            return;
        }
        List<PublishedProduct> publishedProducts = dataManager.load(PublishedProduct.class).
                query("select e from PublishedProduct e where e.site = :site")
                .parameter("site", selectedSite).list();
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
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption(messageBundle.getMessage("notificationProductsInCatalog.caption")).show();
            return;
        }

        List<Consumer> consumers = dataManager.load(Consumer.class).query("select e from Consumer e where e.site=:site")
                .parameter("site", selectedSite).list();
        if (consumers != null) {
            if (consumers.size() != 0) {
                notifications.create(Notifications.NotificationType.WARNING).
                        withCaption("Could not remove the catalog as there are consumers on that catalog!").show();
            }
            else {
                sitesTableRemove.execute();
            }
        }
        else {
            sitesTableRemove.execute();
        }
    }
}