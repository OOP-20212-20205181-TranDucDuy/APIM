package com.company.apimanager.screen.sitegateway;

import com.company.apimanager.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@UiController("SiteGateway.edit")
@UiDescriptor("site-gateway-edit.xml")
@EditedEntityContainer("siteGatewayDc")
public class SiteGatewayEdit extends StandardEditor<SiteGateway> {
    @Autowired
    private CollectionLoader<GatewayService> gatewayDl;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private EntityComboBox<GatewayService> gatewayComboBox;
    @Autowired
    private Notifications notifications;

    @Subscribe
    public void onInitEntity(InitEntityEvent<SiteGateway> event) {
        gatewayDl.setParameter("user", (User) currentAuthentication.getUser());
        VirtualArea virtualArea = getEditedEntity().getSite().getVirtual_area();
        gatewayDl.setParameter("area", virtualArea);

        Site thatSite = getEditedEntity().getSite();
        List<SiteGateway> siteGatewayList = dataManager.load(SiteGateway.class).query("select e from SiteGateway e" +
                " where e.site = :site").parameter("site", thatSite).list();
        if (siteGatewayList != null) {
            List<GatewayService> gatewayServiceList = new ArrayList<GatewayService>(siteGatewayList.size() + 1);
            for (int i = 0; i < siteGatewayList.size(); i++) {
                gatewayServiceList.add(siteGatewayList.get(i).getGateway());
            }
            gatewayServiceList.add(thatSite.getGateway()); //default gateway
            gatewayDl.setParameter("gatewayList", gatewayServiceList);
        }
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (gatewayComboBox.getOptions().getOptions().count() == 0) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption("There is no new gateway to add").show();
        }
    }

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        if (gatewayComboBox.getValue() != null) {
            getEditedEntity().setGateway(gatewayComboBox.getValue());
            getEditedEntity().setName(gatewayComboBox.getValue().getName());
            getEditedEntity().setSite_gateway_id(getEditedEntity().getSite().getId().toString()
                    + "_" + gatewayComboBox.getValue().getId().toString());
        }
    }
}