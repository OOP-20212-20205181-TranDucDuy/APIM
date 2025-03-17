package com.company.apimanager.screen.archivedproducts;

import com.company.apimanager.entity.PublishedProduct;
import io.jmix.core.DataManager;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("ArchivedProductsScreen")
@UiDescriptor("archived-products-screen.xml")
public class ArchivedProductsScreen extends Screen {
    @Autowired
    private CollectionLoader<PublishedProduct> publishedProductsDl;
    @Autowired
    private GroupTable<PublishedProduct> publishedProductsTable;
    @Autowired
    private DataManager dataManager;

    @Subscribe
    public void onInit(InitEvent event) {
        publishedProductsDl.setParameter("archive_state", "ARCHIVED");
    }

    @Subscribe("unArchiveBtn")
    public void onUnArchiveBtnClick(Button.ClickEvent event) {
        PublishedProduct publishedProduct = publishedProductsTable.getSingleSelected();
        if (publishedProduct == null) {
            return;
        }
        publishedProduct.setState("RETIRED");
        dataManager.save(publishedProduct);
        publishedProductsDl.load();
    }
}