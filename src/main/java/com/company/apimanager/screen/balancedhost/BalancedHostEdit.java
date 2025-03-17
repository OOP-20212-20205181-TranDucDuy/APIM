package com.company.apimanager.screen.balancedhost;

import io.jmix.core.EntityStates;
import io.jmix.ui.component.TextField;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.BalancedHost;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("BalancedHost.edit")
@UiDescriptor("balanced-host-edit.xml")
@EditedEntityContainer("balancedHostDc")
public class BalancedHostEdit extends StandardEditor<BalancedHost> {
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private TextField<Long> weightField;

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (entityStates.isNew(getEditedEntity())) {
            weightField.setValue(100L);
        }
    }

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        getEditedEntity().setName(getEditedEntity().getHostname() + ":" + getEditedEntity().getHost_port().toString());
        getEditedEntity().setApi_host(getEditedEntity().getRestApi().getId().toString() +
                        "_" + getEditedEntity().getName());
    }

}