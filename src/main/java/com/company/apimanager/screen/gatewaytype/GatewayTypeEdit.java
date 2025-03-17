package com.company.apimanager.screen.gatewaytype;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.GatewayType;

@UiController("GatewayType.edit")
@UiDescriptor("gateway-type-edit.xml")
@EditedEntityContainer("gatewayTypeDc")
public class GatewayTypeEdit extends StandardEditor<GatewayType> {
}