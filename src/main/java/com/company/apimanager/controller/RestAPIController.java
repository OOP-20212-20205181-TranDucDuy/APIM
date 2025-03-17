package com.company.apimanager.controller;

import com.company.apimanager.entity.PublishedProduct;
import com.company.apimanager.entity.PublishedRestApi;
import io.jmix.core.DataManager;
import io.jmix.core.EntitySerialization;
import io.jmix.core.EntitySerializationOption;
import io.jmix.core.security.Authenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/rest-api")
public class RestAPIController {
    @Autowired
    private DataManager dataManager;
    @Autowired
    private EntitySerialization entitySerialization;
    private final Logger log = LoggerFactory.getLogger(RestAPIController.class);
    @Authenticated
    @CrossOrigin(origins = "*")
    @GetMapping("/detail")
    public String restInfo(
            @RequestParam UUID rest_api_id
    ) {
        try {
            PublishedRestApi restApi = dataManager.load(PublishedRestApi.class).id(rest_api_id).one();
            return entitySerialization.toJson(
                    restApi,
                    null,
                    EntitySerializationOption.DO_NOT_SERIALIZE_DENIED_PROPERTY
            );

        } catch (IllegalStateException illegalStateException) {
            log.error("Not found result in PublishedProductController");
        } catch (Exception e) {
            log.error("Error when get published_product info");
            log.error(e.getMessage());
        }
        return entitySerialization.toJson(
                "{\"success\": \" " + false + "\"}",
                null,
                EntitySerializationOption.DO_NOT_SERIALIZE_DENIED_PROPERTY
        );
    }
}
