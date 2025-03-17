package com.company.apimanager.screen.publishedrestapi;

import com.company.apimanager.entity.PublishedProduct;
import com.company.apimanager.entity.Site;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.PublishedRestApi;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("PublishedRestApi.browse")
@UiDescriptor("published-rest-api-browse.xml")
@LookupComponent("publishedRestApisTable")
public class PublishedRestApiBrowse extends StandardLookup<PublishedRestApi> {
    @Autowired
    private CollectionContainer<PublishedRestApi> publishedRestApisDc;
    @Autowired
    private CollectionLoader<PublishedRestApi> publishedRestApisDl;

    private PublishedProduct product;
    public void setInitParameter(PublishedProduct product){
        this.product = product;
        publishedRestApisDl.setParameter("product", product);
    }

}