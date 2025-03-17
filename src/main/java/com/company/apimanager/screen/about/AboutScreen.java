package com.company.apimanager.screen.about;

import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.Label;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@UiController("AboutScreen")
@UiDescriptor("about-screen.xml")
public class AboutScreen extends Screen {
    @Autowired
    private Label aboutLabel;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {

        var request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String clientIP = request.getRemoteAddr(); // client or last proxy IP


        String value = "<b><font size=\"3\">"

                + "FPT Cloud's API Management product "
                + "</font><b/>";

        value = value + ". IP: " + clientIP;

        aboutLabel.setHtmlEnabled(true);
        aboutLabel.setHtmlSanitizerEnabled(true);
        aboutLabel.setValue(value);
    }
}