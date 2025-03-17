package com.company.apimanager.controller;

import com.company.apimanager.app.KongConnector;
import com.company.apimanager.app.KongServiceObjIds;
import com.company.apimanager.entity.PublishedProduct;
import com.company.apimanager.entity.PublishedRestApi;
import com.company.apimanager.entity.Site;
import com.company.apimanager.screen.brandlogin.ApiRegister;
import com.company.apimanager.screen.publishedproduct.HandledError;
import com.company.apimanager.screen.publishedproduct.PublishedProductBrowse;
import io.jmix.core.DataManager;
import io.jmix.core.EntitySerialization;
import io.jmix.core.EntitySerializationOption;
import io.jmix.core.security.Authenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/published-api")
public class PublishedRestApiController {
    @Autowired
    private DataManager dataManager;

    @Autowired
    private EntitySerialization entitySerialization;

    private final Logger log = LoggerFactory.getLogger(PublishedProductController.class);

    @Authenticated
    @CrossOrigin(origins = "*")
    @GetMapping("/detail")
    public String publishedApiInfo(
            @RequestParam String path
    ) {
        try {
            PublishedRestApi publishedRestApi = dataManager.load(PublishedRestApi.class)
                    .query("select e from PublishedRestApi e where e.path = :path")
                    .parameter("path", path)
                    .one();

            return entitySerialization.toJson(
                    publishedRestApi,
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
