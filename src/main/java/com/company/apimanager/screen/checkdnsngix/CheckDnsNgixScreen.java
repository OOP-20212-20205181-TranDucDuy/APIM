package com.company.apimanager.screen.checkdnsngix;

import com.company.apimanager.app.Utility;
import com.company.apimanager.screen.publishedproduct.HandledError;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.TextField;
import io.jmix.ui.navigation.UrlRouting;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("CheckDnsNgixScreen")
@UiDescriptor("check-DNS_Ngix-screen.xml")
public class CheckDnsNgixScreen extends Screen {
    @Autowired
    private Notifications notifications;
    @Autowired
    private TextField dnsHostNameField;
    @Autowired
    private Utility utility;

    @Subscribe("dnsBtn")
    public void onDnsBtnClick(Button.ClickEvent event) {
        String dnsHostName = dnsHostNameField.getValue().toString();
        String createDnsEntryApi = utility.getAppConfigValue("createDomainInDnsApi");

        HandledError handledError = new HandledError();
        int retInt = Utility.createDnsEntry(createDnsEntryApi, dnsHostName, handledError);
        if (retInt == 200) {        //OK
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption("OK:" + createDnsEntryApi).show();
        }
        else {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(handledError.getErrorMsg()).show();
        }
    }

    @Subscribe("nginxBtn")
    public void onNginxBtnClick(Button.ClickEvent event) {
        String dnsHostName = dnsHostNameField.getValue().toString();
        String createDomainInNginxApi = utility.getAppConfigValue("createDomainInNginxApi");

        HandledError handledError = new HandledError();
        int retInt = Utility.createDnsEntry(createDomainInNginxApi, dnsHostName, handledError);
        if (retInt == 200) {        //OK
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption("OK: " + createDomainInNginxApi).show();
        }
        else {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(handledError.getErrorMsg()).show();
        }
    }

}