package com.company.apimanager.screen.gatewayanalyticsassociation;

import com.company.apimanager.entity.AnalyticsService;
import com.company.apimanager.entity.GatewayService;
import com.company.apimanager.entity.VirtualArea;
import io.jmix.core.DataManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("GatewayAnalyticsAssociation")
@UiDescriptor("gateway-analytics-association.xml")
public class GatewayAnalyticsAssociation extends Screen {
    @Autowired
    private CollectionContainer<GatewayService> gatewayServicesDc;
    @Autowired
    private CollectionLoader<GatewayService> gatewayServicesDl;
    @Autowired
    private EntityComboBox<GatewayService>
            gatewaySelector;
    @Autowired
    private CollectionLoader<AnalyticsService> analyticsServicesDl;
    @Autowired
    private EntityComboBox<AnalyticsService> analyticsSelector;
    @Autowired
    private CollectionLoader<VirtualArea> virtualAreasDl;
    @Autowired
    private CollectionContainer<VirtualArea> virtualAreasDc;
    @Autowired
    private EntityComboBox<VirtualArea> areaSelector;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Button associationBtn;

    @Subscribe("closeAction")
    public void onCloseAction(Action.ActionPerformedEvent event) {
        close(StandardOutcome.CLOSE);
    }

    @Subscribe("areaSelector")
    public void onAreaSelectorValueChange(HasValue.ValueChangeEvent<VirtualArea> event) {
        gatewaySelector.setValue(null);
        gatewayServicesDl.setParameter("area", event.getValue());
        gatewayServicesDl.load();

        analyticsSelector.setValue(null);
        analyticsServicesDl.setParameter("area", event.getValue());
        analyticsServicesDl.load();

        associationBtn.setEnabled(false);
    }

    @Subscribe("associationAction")
    public void onAssociationAction(Action.ActionPerformedEvent event) {
        GatewayService gatewayService = gatewaySelector.getValue();
        AnalyticsService analyticsService = analyticsSelector.getValue();
        if((gatewayService == null) || (analyticsService == null)) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Gateway or Analytics is Null").show();
        }
        else {
            gatewayService.setAnalyticsAssociated(analyticsService);
            dataManager.save(gatewayService);
            notifications.create(Notifications.NotificationType.HUMANIZED).
                    withCaption("Association done!: " + gatewayService.getName() + " with "
                            + analyticsService.getName())
                    .show();
            associationBtn.setEnabled(false);
        }
    }

    @Subscribe("gatewaySelector")
    public void onGatewaySelectorValueChange(HasValue.ValueChangeEvent<GatewayService> event) {
        GatewayService gatewayService = event.getValue();
        if (gatewayService != null) {
            AnalyticsService analyticsService = gatewayService.getAnalyticsAssociated();
            if (analyticsService != null) {
                analyticsSelector.setValue(analyticsService);
            }
            else {
                analyticsSelector.setValue(null);
            }
        }
        associationBtn.setEnabled(false);
    }

    @Subscribe("analyticsSelector")
    public void onAnalyticsSelectorValueChange(HasValue.ValueChangeEvent<AnalyticsService> event) {
        if (analyticsSelector.getValue() != null) {
            AnalyticsService analyticsService = analyticsSelector.getValue();
            GatewayService gatewayService = gatewaySelector.getValue();
            if (gatewayService != null) {
                if (gatewayService.getAnalyticsAssociated() != null) {
                    if (!gatewayService.getAnalyticsAssociated().equals(analyticsService)) {
                        associationBtn.setEnabled(true);
                    }
                    else {
                        associationBtn.setEnabled(false);
                    }
                }
                else {
                    associationBtn.setEnabled(true);
                }
            }
            else  {
                associationBtn.setEnabled(false);
            }
        }
        else {
            associationBtn.setEnabled(false);
        }
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (virtualAreasDc.getItems() != null) {
            if ( virtualAreasDc.getItems().size() != 0) {
                VirtualArea virtualArea = virtualAreasDc.getItems().get(0);
                if (virtualArea != null) {
                    areaSelector.setValue(virtualArea);
                }
            }
        }
        associationBtn.setEnabled(false);
    }
}