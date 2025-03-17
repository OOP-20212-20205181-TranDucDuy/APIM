package com.company.apimanager.screen.gatewaytype;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.GatewayType;

@UiController("GatewayType.browse")
@UiDescriptor("gateway-type-browse.xml")
@LookupComponent("gatewayTypesTable")
public class GatewayTypeBrowse extends StandardLookup<GatewayType> {
}