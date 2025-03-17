package com.company.apimanager.screen.selfhostedgateway;

import com.company.apimanager.entity.User;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.SelfHostedGateway;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("SelfHostedGateway.browse")
@UiDescriptor("self-hosted-gateway-browse.xml")
@LookupComponent("selfHostedGatewaysTable")
public class SelfHostedGatewayBrowse extends StandardLookup<SelfHostedGateway> {
    @Autowired
    private CollectionLoader<SelfHostedGateway> selfHostedGatewaysDl;
    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Subscribe
    public void onInit(InitEvent event) {
        selfHostedGatewaysDl.setParameter("user", (User) currentAuthentication.getUser());
    }
}