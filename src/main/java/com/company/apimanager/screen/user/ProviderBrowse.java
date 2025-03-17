package com.company.apimanager.screen.user;

import com.company.apimanager.app.Utility;
import com.company.apimanager.entity.GatewayUsagePermission;
import com.company.apimanager.entity.PortalUsagePermission;
import com.company.apimanager.screen.gatewayusagepermission.GatewayUsagePermissionBrowse;
import com.company.apimanager.screen.portalusagepermission.PortalUsagePermissionBrowse;
import com.company.apimanager.screen.site.SiteBrowse;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.Screens;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("Provider.browse")
@UiDescriptor("Provider-browse.xml")
@LookupComponent("usersTable")
public class ProviderBrowse extends StandardLookup<User> {
    @Autowired
    private Notifications notifications;
    @Autowired
    private Utility utility;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private CollectionLoader<User> usersDl1;
    @Autowired
    private Screens screens;
    @Autowired
    private GroupTable<User> usersTable;
    @Autowired
    private Button disableBtn;
    @Autowired
    private Button portalBtn;
    @Autowired
    private Button gatewayBtn;
    @Autowired
    private Button siteBtn;
    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Subscribe
    public void onInit(InitEvent event) {
        usersDl1.setParameter("code", "api_provider_owner");
        disableBtn.setEnabled(false);
        portalBtn.setEnabled(false);
        gatewayBtn.setEnabled(false);
        siteBtn.setEnabled(false);
    }

    @Subscribe("usersTable")
    public void onUsersTableSelection(Table.SelectionEvent<User> event) {
        if (usersTable.getSingleSelected() == null) {
            disableBtn.setEnabled(false);
            portalBtn.setEnabled(false);
            gatewayBtn.setEnabled(false);
        }
        else {
            User selectedUser = usersTable.getSingleSelected();
            if (selectedUser.getProviderPlan().equals("Enterprise")) {
                portalBtn.setEnabled(true);
                gatewayBtn.setEnabled(true);
                siteBtn.setEnabled(false);
            }
            else {
                portalBtn.setEnabled(false);
                gatewayBtn.setEnabled(false);
                siteBtn.setEnabled(true);
            }
            disableBtn.setEnabled(true);

        }
    }

    @Subscribe
    public void onBeforeClose(BeforeCloseEvent event) {
        //notifications.create().withCaption(string).show();
        //event.preventWindowClose();
    }

    @Subscribe("gatewayBtn")
    public void onGatewayBtnClick(Button.ClickEvent event) {
        if (usersTable.getSingleSelected() == null) {
            return;
        }
        GatewayUsagePermissionBrowse gatewayUsagePermissionBrowse = screens.create(GatewayUsagePermissionBrowse.class);
        gatewayUsagePermissionBrowse.setUser(usersTable.getSingleSelected());
        gatewayUsagePermissionBrowse.show();
    }

    @Subscribe("portalBtn")
    public void onPortalBtnClick(Button.ClickEvent event) {
        if (usersTable.getSingleSelected() == null) {
            return;
        }
        PortalUsagePermissionBrowse portalUsagePermissionBrowse =
                screens.create(PortalUsagePermissionBrowse.class);
        portalUsagePermissionBrowse.setUser(usersTable.getSingleSelected());
        portalUsagePermissionBrowse.show();
    }

    @Subscribe("siteBtn")
    public void onSiteBtnClick(Button.ClickEvent event) {
        User selectedUser = usersTable.getSingleSelected();
        if (selectedUser == null) {
            return;
        }
        SiteBrowse siteBrowse = screens.create(SiteBrowse.class);
        siteBrowse.setSiteOwner(selectedUser);
        siteBrowse.show();
    }

}