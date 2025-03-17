package com.company.apimanager.screen.virtualarea;

import io.jmix.ui.Screens;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.VirtualArea;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("VirtualArea.browse")
@UiDescriptor("virtual-area-browse.xml")
@LookupComponent("virtualAreasTable")
public class VirtualAreaBrowse extends StandardLookup<VirtualArea> {
    @Autowired
    private GroupTable<VirtualArea> virtualAreasTable;
    @Autowired
    private Screens screens;
    @Autowired
    private Button createGwBtn;
    @Autowired
    private HBoxLayout lookupActions;
    @Autowired
    private Button portalBtn;
    @Autowired
    private Button analyticsBtn;
    @Autowired
    private Button createBtn;
    @Autowired
    private Button editBtn;
    @Autowired
    private Button removeBtn;
    @Autowired
    private CollectionLoader<VirtualArea> virtualAreasDl;

    @Subscribe("portalAction")
    public void onPortalAction(Action.ActionPerformedEvent event) {
        VirtualArea virtualArea = virtualAreasTable.getSingleSelected();
        if (virtualArea != null) {
            VirtualAreaEdit virtualAreaEdit = screens.create(VirtualAreaEdit.class);
            virtualAreaEdit.setEntityToEdit(virtualArea);
            virtualAreaEdit.setMode("portal");
            virtualAreaEdit.show();
        }
    }

    @Subscribe("analyticsAction")
    public void onAnalyticsAction(Action.ActionPerformedEvent event) {
        VirtualArea virtualArea = virtualAreasTable.getSingleSelected();
        if (virtualArea != null) {
            VirtualAreaEdit virtualAreaEdit = screens.create(VirtualAreaEdit.class);
            virtualAreaEdit.setEntityToEdit(virtualArea);
            virtualAreaEdit.setMode("analytics");
            virtualAreaEdit.show();
        }
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (lookupActions.isVisible()) { //Từ màn hình khác lookup để chọn Virtual Area, ví dụ từ Site.edit
            createGwBtn.setVisible(false);
            portalBtn.setVisible(false);
            analyticsBtn.setVisible(false);
            createBtn.setVisible(false);
            editBtn.setVisible(false);
            removeBtn.setVisible(false);

            virtualAreasDl.setParameter("is_active", true);
            virtualAreasDl.load();
        }
    }

    @Subscribe("createGwAction")
    public void onCreateGwAction(Action.ActionPerformedEvent event) {
        VirtualArea virtualArea = virtualAreasTable.getSingleSelected();
        if (virtualArea != null) {
            VirtualAreaEdit virtualAreaEdit = screens.create(VirtualAreaEdit.class);
            virtualAreaEdit.setEntityToEdit(virtualArea);
            virtualAreaEdit.setMode("gateway");
            virtualAreaEdit.show();
        }
    }

    @Subscribe
    public void onInit(InitEvent event) {
        createGwBtn.setEnabled(false);
        portalBtn.setEnabled(false);
        analyticsBtn.setEnabled(false);
    }

    @Subscribe("virtualAreasTable")
    public void onVirtualAreasTableSelection(Table.SelectionEvent<VirtualArea> event) {
        createGwBtn.setEnabled(true);
        portalBtn.setEnabled(true);
        analyticsBtn.setEnabled(true);
    }
}