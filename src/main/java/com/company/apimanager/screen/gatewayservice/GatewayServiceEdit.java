package com.company.apimanager.screen.gatewayservice;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.GatewayService;

@UiController("GatewayService.edit")
@UiDescriptor("gateway-service-edit.xml")
@EditedEntityContainer("gatewayServiceDc")
public class GatewayServiceEdit extends StandardEditor<GatewayService> {
}