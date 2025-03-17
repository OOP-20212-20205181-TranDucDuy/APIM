package com.company.apimanager.controller;

import com.company.apimanager.app.EncodePassword;
import com.company.apimanager.app.RestClient;
import com.company.apimanager.dto.CheckRequest;
import com.company.apimanager.dto.LoginRequest;
import com.company.apimanager.entity.Consumer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.jmix.core.DataManager;
import io.jmix.core.EntitySerialization;
import io.jmix.core.EntitySerializationOption;
import io.jmix.core.security.Authenticated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/authenticate")
public class AutheticateController {
    @Autowired
    private DataManager dataManager;

    @Autowired
    private EntitySerialization entitySerialization;
    private final Logger log = LoggerFactory.getLogger(PublishedProductController.class);
    @Authenticated
    @CrossOrigin(origins = "*")
    @PostMapping("/authenticated_userid")
    public String getAutheticatedUserId(
           @RequestParam String client_id
    ) {
        try {
            Consumer consumer = dataManager.load(Consumer.class)
                    .query("select e from Consumer e where e.oauth_client_id = :client_id")
                    .parameter("client_id", client_id)
                    .one();

            return entitySerialization.toJson(
                    consumer,
                    null,
                    EntitySerializationOption.DO_NOT_SERIALIZE_DENIED_PROPERTY
            );
        } catch (IllegalStateException illegalStateException) {
            log.error("Not found result in AutheticateController");
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
