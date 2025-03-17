package com.company.apimanager.controller;
import com.company.apimanager.entity.Site;
import io.jmix.core.DataManager;
import io.jmix.core.EntitySerialization;
import io.jmix.core.EntitySerializationOption;
import io.jmix.core.security.Authenticated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/sites")
public class SiteController {
    @Autowired
    private DataManager dataManager;

    @Autowired
    private EntitySerialization entitySerialization;

    @Authenticated
    @CrossOrigin(origins = "*")
    @GetMapping("/detail")
    public String siteInfo(
            @RequestParam UUID site_id
    ) {
        try {
            Site site = dataManager.load(Site.class).id(site_id).one();

            return entitySerialization.toJson(
                    site,
                    null,
                    EntitySerializationOption.DO_NOT_SERIALIZE_DENIED_PROPERTY
            );
        } catch (IllegalStateException illegalStateException) {
            return entitySerialization.toJson(
                    new Object(),
                    null,
                    EntitySerializationOption.DO_NOT_SERIALIZE_DENIED_PROPERTY
            );
        }
    }
    @Authenticated
    @CrossOrigin(origins = "*")
    @GetMapping("/all")
    public String allSites() {
        try {
            return entitySerialization.toJson(
                    dataManager.load(Site.class).all().list(),
                    null,
                    EntitySerializationOption.DO_NOT_SERIALIZE_DENIED_PROPERTY
            );
        } catch (IllegalStateException illegalStateException) {
            return entitySerialization.toJson(
                    new Object(),
                    null,
                    EntitySerializationOption.DO_NOT_SERIALIZE_DENIED_PROPERTY
            );
        }
    }
}
