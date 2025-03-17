package com.company.apimanager.screen.restapi;

import com.company.apimanager.entity.BalancedHost;
import com.company.apimanager.entity.Product;
import com.company.apimanager.entity.User;
import com.company.apimanager.screen.brandlogin.ApiRegister;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.RestApi;
import io.jmix.ui.screen.LookupComponent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.List;

@UiController("RestApi.browse")
@UiDescriptor("rest-api-browse.xml")
@LookupComponent("restApisTable")
public class RestApiBrowse extends StandardLookup<RestApi> {
    @Autowired
    private CollectionLoader<RestApi> restApisDl;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private HBoxLayout lookupActions;
    @Autowired
    private Button createBtn;
    @Autowired
    private Button editBtn;
    @Autowired
    private Button removeBtn;

    @Autowired
    private GroupTable<RestApi> restApisTable;

    private String providerPlan;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Notifications notifications;
    @Named("restApisTable.remove")
    private RemoveAction<RestApi> restApisTableRemove;
    @Autowired
    private Button remove1Btn;

    @Subscribe
    public void onInit(InitEvent event) {
        User currUser = (User) currentAuthentication.getUser();
        restApisDl.setParameter("owner", currUser);

        providerPlan = currUser.getProviderPlan();
        if (providerPlan == null) {
            providerPlan = "Pilot";
        }
    }

    @Subscribe("restApisTable")
    public void onRestApisTableSelection(Table.SelectionEvent<RestApi> event) {
        remove1Btn.setEnabled(true);
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (lookupActions.isVisible()) {
            createBtn.setVisible(false);
            editBtn.setVisible(false);
            removeBtn.setVisible(false);
            remove1Btn.setVisible(false);
        }
        else {
            if (providerPlan.equals("Pilot")) {
                if (restApisTable.getItems().size() > 1) {
                    createBtn.setEnabled(false);
                }
                else {
                    createBtn.setEnabled(true);
                }
            }
            remove1Btn.setEnabled(false);
        }
    }

    @Install(to = "restApisTable.create", subject = "afterCommitHandler")
    private void restApisTableCreateAfterCommitHandler(RestApi restApi) {
        if (providerPlan.equals("Pilot")) {
            if (restApisTable.getItems().size() > 1) {
                createBtn.setEnabled(false);
            }
            else {
                createBtn.setEnabled(true);
            }
        }
    }

    @Install(to = "restApisTable.remove", subject = "afterActionPerformedHandler")
    private void restApisTableRemoveAfterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent<RestApi> afterActionPerformedEvent) {
        if (providerPlan.equals("Pilot")) {
            if (restApisTable.getItems().size() > 1) {
                createBtn.setEnabled(false);
            }
            else {
                createBtn.setEnabled(true);
            }
        }

        if (restApisTable.getItems().size() == 0) {
            remove1Btn.setEnabled(false);
        }
    }

    @Subscribe("remove1Btn")
    public void onRemove1BtnClick(Button.ClickEvent event) {
        RestApi selectedRestApi = restApisTable.getSingleSelected();
        if (selectedRestApi == null) {
            return;
        }
        List<ApiRegister> apiRegisters =  dataManager.load(ApiRegister.class).
                query("select e from ApiRegister e where e.api = :api")
                .parameter("api", selectedRestApi).list();
        boolean noProducts = false;
        if (apiRegisters == null) {
            noProducts = true;
        }
        else {
            if (apiRegisters.size() == 0) {
                noProducts = true;
            }
        }
        if (noProducts) {
            List<BalancedHost> balancedHosts = dataManager.load(BalancedHost.class).
                    query("select e from BalancedHost e where e.restApi = :api")
                    .parameter("api", selectedRestApi).list();
            boolean noBalancedHost = false;
            if (balancedHosts == null) {
                noBalancedHost = true;
            }
            else {
                if (balancedHosts.size() == 0) {
                    noBalancedHost = true;
                }
            }
            if (noBalancedHost) {
                restApisTableRemove.execute();
            }
            else {
                notifications.create(Notifications.NotificationType.WARNING).
                        withCaption("Could not remove the API as there are backend hosts contained that API!").show();
            }
        }
        else {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Could not remove the API as there are products contain that API!").show();
        }
    }

}