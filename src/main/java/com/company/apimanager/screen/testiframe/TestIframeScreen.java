package com.company.apimanager.screen.testiframe;

import io.jmix.ui.component.BrowserFrame;
import io.jmix.ui.navigation.UrlRouting;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("TestIframeScreen")
@UiDescriptor("test-iframe-screen.xml")
public class TestIframeScreen extends Screen {
    @Autowired
    private BrowserFrame frame1;

    @Subscribe
    public void onInit(InitEvent event) {
    }
}