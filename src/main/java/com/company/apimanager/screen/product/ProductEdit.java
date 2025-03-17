package com.company.apimanager.screen.product;

import com.company.apimanager.entity.User;
import io.jmix.core.EntityStates;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.TextField;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("Product.edit")
@UiDescriptor("product-edit.xml")
@EditedEntityContainer("productDc")
public class ProductEdit extends StandardEditor<Product> {
    @Autowired
    private InstanceContainer<Product> productDc;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private TextField<String> nameField;

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (!entityStates.isNew(getEditedEntity())) {
            nameField.setEditable(false);
        }
    }

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        User user = (User) currentAuthentication.getUser();
        Product product = getEditedEntity();
        product.setOwner(user);
        product.setOwner_prdname(user.getUsername() + "_" + product.getName());
    }
}