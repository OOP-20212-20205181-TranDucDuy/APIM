package com.company.apimanager.screen.testpage;

import com.company.apimanager.app.KongAclDetails;
import com.company.apimanager.app.KongConnector;
import com.company.apimanager.app.KongRouteDetails;
import com.company.apimanager.app.KongServiceDetails;
import com.company.apimanager.screen.publishedproduct.HandledError;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("TestpageScreen")
@UiDescriptor("testpage-screen.xml")
public class TestpageScreen extends Screen {
    @Autowired
    private Notifications notifications;

    @Subscribe("testAction")
    public void onTestAction(Action.ActionPerformedEvent event) {
        HandledError handledError = new HandledError();
        KongConnector kongConnector = new KongConnector("http://localhost:8001");
        /*
        KongServiceDetails kongServiceDetails = kongConnector.getServicebyName("todos");
        notifications.create(Notifications.NotificationType.HUMANIZED).withCaption(kongServiceDetails.getHost() +
                ":" + kongServiceDetails.getPath() + ": " + kongServiceDetails.getPort()).show();

         */
        //KongRouteDetails kongRouteDetails = kongConnector.getRoutebyName("route2", handledError);

        /*notifications.create(Notifications.NotificationType.HUMANIZED).withCaption(kongRouteDetails.getId() +
                ":" + kongRouteDetails.getName()).show(); */
        /*
        String[] groupList = new String[2];
        groupList[0] = "group1";
        groupList[1] = "group2";
        int intRet = kongConnector.createAcl("mock_io_service", groupList, handledError);
        notifications.create(Notifications.NotificationType.HUMANIZED).withCaption(intRet + ": "
        + handledError.getErrorMsg()).show();
         */
        /*kongConnector.updateRateLimit(handledError);
        notifications.create(Notifications.NotificationType.HUMANIZED).
                withCaption(handledError.getErrorMsg()).show();
         */
        KongAclDetails kongAclDetails = new KongAclDetails();
        kongConnector.getAclFromService("mock_io_service", kongAclDetails, handledError);
        /*kongConnector.getAcl("mock_io_service", "35572ded-1807-4fcd-b032-280503a7fe13",
                kongAclDetails, handledError);

         */

        /*
        String[] currList = kongAclDetails.getGroupList();
        String[] newList = new String[currList.length + 1];
        for (int i = 0; i < currList.length; i++) {
            newList[i] = currList[i];
        }
        newList[currList.length] = "NewGroup_123";
        kongAclDetails.setGroupList(newList);
        kongConnector.updateAcl("mock_io_service", "35572ded-1807-4fcd-b032-280503a7fe13", kongAclDetails,
                handledError);

         */
    }
}