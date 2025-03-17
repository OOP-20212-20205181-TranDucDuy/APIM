package com.company.apimanager.messaging;
import com.company.apimanager.app.KongConsumerGroupController;
import com.company.apimanager.app.Utility;
import com.company.apimanager.app.kong_object.KongConsumerGroup;
import com.company.apimanager.app.kong_object.KongConsumerGroupRatelimit;
import com.company.apimanager.entity.GatewayService;
import com.company.apimanager.entity.User;
import com.company.apimanager.screen.publishedproduct.HandledError;
import io.jmix.core.DataManager;
import io.jmix.core.security.Authenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PricingPlanReceiver {
    private static final Logger logger = LoggerFactory.getLogger(PricingPlanReceiver.class);
    @Autowired
    Utility utility;
    @Autowired
    DataManager dataManager;
    //@RabbitListener(queues = "${spring.rabbitmq.pricing_plan_queue_name}", concurrency = "${spring.rabbitmq.listener.direct.consumers-per-queue}")
    @Authenticated
    public void receivedMessage(Map<String, Object> map) {
        User owner = (User) map.get("owner");
        GatewayService gateway =  (GatewayService) map.get("gateway");
        logger.info("map Details Received is.. " + map);

        KongConsumerGroupController consumerGroupController = new KongConsumerGroupController((gateway.getManagement_endpoint()));
        String groupName = owner.getUsername();
        HandledError getConsumerGroupHandledError = new HandledError();
        KongConsumerGroup group = consumerGroupController.getConsumerGroupByName(groupName, getConsumerGroupHandledError);
        if (getConsumerGroupHandledError.getHttpResponseCode()!=404 && getConsumerGroupHandledError.getHttpResponseCode()!=200){ //404 group not exist
            logger.error("Get Consumer Group: "+ getConsumerGroupHandledError.getErrorMsg());
        }
        // so create new group;
        if (getConsumerGroupHandledError.getHttpResponseCode() == 404){ //group not exist => create new and set limit
            HandledError createConsumerGroupHandledError = new HandledError();
            group = consumerGroupController.createConsumerGroup(groupName, createConsumerGroupHandledError);
            if (group==null){
                logger.error("Create Consumer Group: "+ createConsumerGroupHandledError.getErrorMsg());
            }

            KongConsumerGroupRatelimit groupRatelimit = new KongConsumerGroupRatelimit(owner.getPricingPlan().getRate_limit());
            HandledError setLimitOnGroupHandledError = new HandledError();
            int setRateLimitResult = consumerGroupController.setRateLimitOnGroup(group, groupRatelimit, setLimitOnGroupHandledError);
            if (setRateLimitResult!=201){
                logger.error("Add Limit: "+ setLimitOnGroupHandledError.getErrorMsg());
            }
        }

        if (getConsumerGroupHandledError.getHttpResponseCode() == 200){ //group existed => update ratelimit
            KongConsumerGroupRatelimit groupRatelimit = new KongConsumerGroupRatelimit(owner.getPricingPlan().getRate_limit());
            HandledError updateLimitOnGroupHandledError = new HandledError();
            int updateRateLimitResult = consumerGroupController.updateRateLimitOnGroup(group, groupRatelimit, updateLimitOnGroupHandledError);
            if (updateRateLimitResult!=200){
                logger.error("Add Limit: "+ updateLimitOnGroupHandledError.getErrorMsg());
            }
        }


    }
}
