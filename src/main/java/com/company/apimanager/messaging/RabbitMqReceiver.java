package com.company.apimanager.messaging;

import com.company.apimanager.app.Utility;
import com.company.apimanager.entity.GatewayService;
import com.company.apimanager.entity.Site;
import com.company.apimanager.entity.User;
import com.company.apimanager.screen.publishedproduct.HandledError;
import io.jmix.core.DataManager;
import io.jmix.core.security.Authenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RabbitMqReceiver{
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqReceiver.class);
    @Autowired
    Utility utility;
    @Autowired
    DataManager dataManager;
    //@RabbitListener(queues = "${spring.rabbitmq.site_queue_name}", concurrency = "${spring.rabbitmq.listener.direct.consumers-per-queue}")
    @Authenticated
    public void receivedMessage(Map<String, Object> map) {

    }
    public void receivedMessage1(Map<String, Object> map) {
        String url = "";
        User user = (User) map.get("user");
        Site site = (Site) map.get("site");
        logger.info("map Details Received is.. " + map);
        HandledError msg = new HandledError();
        if (user.getPricingPlan()==null){
            logger.error("API Provider " + user.getUsername() +" does not have Pricing Plan - please update!");
        }else{
            if (!user.getPricingPlan().getDeveloper_portal()){ //user can not use dev portal
                // do nothing
                logger.debug("API Provider " + user.getUsername() + " can not use Developer Portal, but still create and hide the URL.");
            }
            try {
                url = Site.createPortal(user.getUsername() + '-' + site.getName().toLowerCase(), site.getPortal().getManagement_endpoint());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }

            if (url.isEmpty()) {
                logger.error("Cannot create site portal");
                return;
            }

            site.setDrupal_api("https://" + url);
            dataManager.save(site);

            if (user.getPricingPlan().getCustom_domain()){ //user can use custom domain
                try {
                    String createGWEndpoint = utility.getAppConfigValue("createGatewayDomain");
                    GatewayService.createGateway(user.getUsername(), site.getName().toLowerCase(), site.getGateway().getIp_address(), site.getDefaultInvocationEndpoint(), createGWEndpoint, msg);
                    if (msg.getErrorMsg() != null) {
                        logger.error("Error when create gw");
                        logger.error(msg.getErrorMsg());
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }

            }else{
                logger.debug("API Provider " + user.getUsername() + " can not use Custom Domain, so not create gateway");
            }
        }
    }
}
