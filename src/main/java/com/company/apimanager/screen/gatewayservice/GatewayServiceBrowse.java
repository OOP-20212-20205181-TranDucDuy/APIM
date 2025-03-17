package com.company.apimanager.screen.gatewayservice;

import io.jmix.ui.Notifications;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("GatewayService.browse")
@UiDescriptor("gateway-service-browse.xml")
@LookupComponent("gatewayServicesTable")
public class GatewayServiceBrowse extends StandardLookup<GatewayService> {

    @Autowired
    private HBoxLayout lookupActions;


}