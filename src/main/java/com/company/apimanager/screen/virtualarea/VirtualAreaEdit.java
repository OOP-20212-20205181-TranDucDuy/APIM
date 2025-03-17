package com.company.apimanager.screen.virtualarea;

import com.company.apimanager.entity.AnalyticsService;
import com.company.apimanager.entity.GatewayService;
import com.company.apimanager.entity.PortalService;
import io.jmix.core.EntityStates;
import io.jmix.ui.component.GroupBoxLayout;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.TextField;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.VirtualArea;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("VirtualArea.edit")
@UiDescriptor("virtual-area-edit.xml")
@EditedEntityContainer("virtualAreaDc")
public class VirtualAreaEdit extends StandardEditor<VirtualArea> {
    @Autowired
    private EntityStates entityStates;

    private Boolean create_new;
    @Autowired
    private Table<GatewayService> gatewaysTable;
    @Autowired
    private Table<PortalService> portalsTable;
    @Autowired
    private Table<AnalyticsService> analyticsTable;
    @Autowired
    private GroupBoxLayout gatewaysBox;
    @Autowired
    private GroupBoxLayout analyticsBox;
    @Autowired
    private GroupBoxLayout portalsBox;

    private  String editMode = "";
    @Autowired
    private TextField<String> nameField;
    @Autowired
    private TextField<String> titleField;
    @Autowired
    private TextField<String> descriptionField;

    public void setMode(String editMode) {
        this.editMode = editMode;
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (entityStates.isNew(getEditedEntity())) {
            create_new = true;
            gatewaysTable.setVisible(false);
            portalsTable.setVisible(false);
            analyticsTable.setVisible(false);
            gatewaysBox.setVisible(false);
            analyticsBox.setVisible(false);
            portalsBox.setVisible(false);
        }
        else {
            create_new = false;
            nameField.setEditable(false);
            DoMode();
        }
    }

    private void  DoMode() {
        switch (editMode) {
            case "gateway":
                gatewaysTable.setVisible(true);
                portalsTable.setVisible(false);
                analyticsTable.setVisible(false);
                gatewaysBox.setVisible(true);
                analyticsBox.setVisible(false);
                portalsBox.setVisible(false);

                nameField.setEditable(false);
                titleField.setEditable(false);
                descriptionField.setEditable(false);
                break;
            case "portal":
                gatewaysTable.setVisible(false);
                portalsTable.setVisible(true);
                analyticsTable.setVisible(false);
                gatewaysBox.setVisible(false);
                analyticsBox.setVisible(false);
                portalsBox.setVisible(true);

                nameField.setEditable(false);
                titleField.setEditable(false);
                descriptionField.setEditable(false);
                break;
            case "analytics":
                gatewaysTable.setVisible(false);
                portalsTable.setVisible(false);
                analyticsTable.setVisible(true);
                gatewaysBox.setVisible(false);
                analyticsBox.setVisible(true);
                portalsBox.setVisible(false);

                nameField.setEditable(false);
                titleField.setEditable(false);
                descriptionField.setEditable(false);
                break;
            default:
                gatewaysTable.setVisible(false);
                portalsTable.setVisible(false);
                analyticsTable.setVisible(false);
                gatewaysBox.setVisible(false);
                analyticsBox.setVisible(false);
                portalsBox.setVisible(false);

                nameField.setEditable(true);
                titleField.setEditable(true);
                descriptionField.setEditable(true);
        }
    }
}