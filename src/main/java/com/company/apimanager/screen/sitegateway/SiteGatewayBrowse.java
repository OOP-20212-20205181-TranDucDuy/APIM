package com.company.apimanager.screen.sitegateway;

import com.company.apimanager.entity.Site;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.SiteGateway;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("SiteGateway.browse")
@UiDescriptor("site-gateway-browse.xml")
@LookupComponent("siteGatewaysTable")
public class SiteGatewayBrowse extends StandardLookup<SiteGateway> {
    private Site site;
    @Autowired
    private CollectionLoader<SiteGateway> siteGatewaysDl;

    public Site getSite() {
        return site;
    }

    @Install(to = "siteGatewaysTable.create", subject = "initializer")
    private void siteGatewaysTableCreateInitializer(SiteGateway siteGateway) {
        siteGateway.setSite(site);
    }

    public void setSite(Site site) {
        this.site = site;
        siteGatewaysDl.setParameter("site", site);
    }
}