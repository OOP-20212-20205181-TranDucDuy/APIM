package com.company.apimanager.screen.main;

import com.company.apimanager.screen.login.LoginScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.company.apimanager.app.CompilerDirective;
import com.company.apimanager.app.Utility;
import com.company.apimanager.entity.Site;
import com.company.apimanager.entity.User;
import com.company.apimanager.screen.publishedproduct.PublishedProductBrowse;
import com.company.apimanager.screen.site.SiteEdit;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.session.SessionData;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenTools;
import io.jmix.ui.Screens;
import io.jmix.ui.component.AppWorkArea;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.mainwindow.Drawer;
import io.jmix.ui.component.mainwindow.SideMenu;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

@UiController("MainScreen")
@UiDescriptor("main-screen.xml")
@Route(path = "main", root = true)
public class MainScreen extends Screen implements Window.HasWorkArea {

    @Autowired
    private ScreenTools screenTools;

    @Autowired
    private AppWorkArea workArea;
    @Autowired
    private Drawer drawer;
    @Autowired
    private Button collapseDrawerButton;
    @Autowired
    private SideMenu sideMenu;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Screens screens;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private SessionData sessionData;

    private final Logger log = LoggerFactory.getLogger(LoginScreen.class);

    @Autowired
    private Utility utility;


    @Override
    public AppWorkArea getWorkArea() {
        return workArea;
    }

    @Subscribe("collapseDrawerButton")
    private void onCollapseDrawerButtonClick(Button.ClickEvent event) {
        drawer.toggle();
        if (drawer.isCollapsed()) {
            collapseDrawerButton.setIconFromSet(JmixIcon.CHEVRON_RIGHT);
        } else {
            collapseDrawerButton.setIconFromSet(JmixIcon.CHEVRON_LEFT);
        }
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        try {
            User currUser = (User) currentAuthentication.getUser();
            List<SideMenu.MenuItem> menuItemList = sideMenu.getMenuItems().get(0).getChildren().get(0).getChildren();
            SideMenu.MenuItem siteMenuItem = menuItemList.get(2);
//            SideMenu.MenuItem selfHostedGwMenuItem = sideMenu.getMenuItems().get(0).getChildren().get(1);
//            SideMenu.MenuItem selfHostedGwMenuItem = sideMenu.getMenuItem("gatewayManager");
//            SideMenu.MenuItem selfHostedGwMenuItem = sideMenu.getMenuItem("SelfHostedGateway.browse");
            if (!currUser.getProviderPlan().equals("Enterprise")) {
//                selfHostedGwMenuItem.setVisible(false);
                siteMenuItem.setCaption("Public site");
                siteMenuItem.setCommand(menuItem -> {
                    try {
                        List<Site> siteList = dataManager.load(Site.class).query("select e from Site e where e.owner = :owner")
                                .parameter("owner", currUser).list();
                        if (siteList == null) {
                            notifications.create(Notifications.NotificationType.WARNING).
                                    withCaption("There is no Site for that provider\r\n. Please contact the Cloud topology admin").show();
                        }
                        else {
                            if (siteList.size() == 0) {
                                notifications.create(Notifications.NotificationType.WARNING).
                                        withCaption("There is no Site for that provider\r\n. Please contact the Cloud topology admin").show();
                            }
                            else {
                                PublishedProductBrowse publishedProductBrowse = screens.create(PublishedProductBrowse.class);
                                publishedProductBrowse.setSite(siteList.get(0));
                                publishedProductBrowse.setArchivedState("ARCHIVED");
                                publishedProductBrowse.show();
                            }
                        }
                    }
                    catch (Exception e) {
                        notifications.create(Notifications.NotificationType.WARNING).
                                withCaption("There is no Site for that provider\r\n. Please contact the Cloud topology admin").show();
                    }
                });
            }
            else {
                if (CompilerDirective.onCloud) {
                    String usingSelfHostedGateway = utility.getAppConfigValue("usingSelfHostedGateway");
                    log.info("Self hosted gateway: " + usingSelfHostedGateway);
//                    selfHostedGwMenuItem.setVisible(false);
//                    if (usingSelfHostedGateway != null) {
//                        if (usingSelfHostedGateway.equalsIgnoreCase("true")) {
//                            selfHostedGwMenuItem.setVisible(true);
//                        }
//                    }
                }
//                else {
//                    selfHostedGwMenuItem.setVisible(false);
//                }
            }
        }
        catch (Exception e) {

        }

        screenTools.openDefaultScreen(
                UiControllerUtils.getScreenContext(this).getScreens());
        screenTools.handleRedirect();
    }
}
