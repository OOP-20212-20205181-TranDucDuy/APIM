package com.company.apimanager.screen.apiregister;

import com.company.apimanager.app.Utility;
import com.company.apimanager.entity.*;
import com.company.apimanager.screen.publishedproduct.HandledError;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Notifications;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.Label;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.apimanager.screen.brandlogin.ApiRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("ApiRegister.browse")
@UiDescriptor("api-register-browse.xml")
@LookupComponent("apiRegistersTable")
public class ApiRegisterBrowse extends StandardLookup<ApiRegister> {
    @Autowired
    private CollectionLoader<ApiRegister> apiRegistersDl;
    private Product product;
    @Autowired
    private Label productLabel;
    @Autowired
    private GroupTable<ApiRegister> apiRegistersTable;

    private String providerPlan;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private Button createBtn;

    @Autowired
    private Notifications notifications;

    @Autowired
    private DataManager dataManager;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public void setProduct(Product product) {
        this.product = product;
        apiRegistersDl.setParameter("product", product);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        apiRegistersDl.setParameter("product", product);

        String value = "<b><font size=\"3\">"
                + "APIs registered in " + product.getName()
                + "</font><b/>";
        productLabel.setHtmlEnabled(true);
        productLabel.setHtmlSanitizerEnabled(true);
        productLabel.setValue(value);
    }

    @Install(to = "apiRegistersTable.create", subject = "initializer")
    private void apiRegistersTableCreateInitializer(ApiRegister apiRegister) {
        apiRegister.setProduct(product);
    }

    @Subscribe("removeBtn")
    public void onRemoveBtnClick(io.jmix.ui.component.Button.ClickEvent event) {
        /*
        try {
            ApiRegister apiRegister = apiRegistersTable.getSingleSelected();
            assert apiRegister != null;
            Product product = apiRegister.getProduct();
            List<PublishedProduct> publishedProducts = dataManager.load(PublishedProduct.class)
                    .query("select p from PublishedProduct p where p.product=:product")
                    .parameter("product", product)
                    .list();

            List<DrupalLink> drupalLinkList = dataManager.load(DrupalLink.class)
                    .condition(LogicalCondition.and(
                            PropertyCondition.inList("publishedProduct", publishedProducts),
                            PropertyCondition.equal("restApi", apiRegister.getApi())
                    ))
                    .list();

            if (!drupalLinkList.isEmpty()) {
                for (DrupalLink drupalLink:drupalLinkList) {
                    if (drupalLink.getDrupal_api_id() != null && !drupalLink.getDrupal_api_id().isBlank()
                    && drupalLink.getKong_api_id() != null && !drupalLink.getKong_api_id().isBlank()) {
                        dataManager.remove(drupalLink);
                    }
                }

            }
        } catch (Exception e) {
            log.error("Error when remove api register");
            log.error(e.getMessage());
        }

         */
    }

}