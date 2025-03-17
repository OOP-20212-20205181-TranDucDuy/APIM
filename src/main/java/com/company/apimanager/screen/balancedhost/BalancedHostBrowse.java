package com.company.apimanager.screen.balancedhost;

import io.jmix.ui.screen.*;
import com.company.apimanager.entity.BalancedHost;

@UiController("BalancedHost.browse")
@UiDescriptor("balanced-host-browse.xml")
@LookupComponent("balancedHostsTable")
public class BalancedHostBrowse extends StandardLookup<BalancedHost> {
}