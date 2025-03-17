package com.company.apimanager.screen.publishedrestapi;

import com.company.apimanager.app.KongConnector;
import com.company.apimanager.app.kong_object.IpRestrictionSecurityPlugin;
import com.company.apimanager.app.kong_object.IpRestrictionSecurityPluginConfig;
import com.company.apimanager.app.kong_object.RequestTransformerTransformationsPlugin;
import com.company.apimanager.entity.Consumer;
import com.company.apimanager.entity.GatewayService;
import com.company.apimanager.entity.Site;
import com.company.apimanager.screen.publishedproduct.HandledError;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.ValuesPicker;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import com.company.apimanager.entity.PublishedRestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

@UiController("PublishedRestApi.edit")
@UiDescriptor("published-rest-api-edit.xml")
@EditedEntityContainer("publishedRestApiDc")
public class PublishedRestApiEdit extends StandardEditor<PublishedRestApi> {
    @Autowired
    private Notifications notifications;
    private HandledError handledError = new HandledError();
    private boolean isNewEntity;
    private boolean enableIpRestriction = false;
    private KongConnector kongConnector;
    @Autowired
    private ValuesPicker<String> vpIpDeny;
    @Autowired
    private ValuesPicker<String> vpIpAllow;
    @Autowired
    private CheckBox enableIpLimitation;

    @Subscribe
    public void onInitEntity(InitEntityEvent<Site> event) {
        isNewEntity = true;
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (!isNewEntity) {
            kongConnector = new KongConnector(getEditedEntity().getProduct().getSite().getGateway().getManagement_endpoint());
            if (StringUtils.hasText(getEditedEntity().getPlugin())) {
                IpRestrictionSecurityPlugin ipRestrictionSecurityPlugin = getIpRestrictionFromDB(getEditedEntity().getPlugin());
                if (ipRestrictionSecurityPlugin != null) {
                    enableIpLimitation.setValue(true);
                    setVisibleIpLimit(true);
                    setIpLimitationForUI(ipRestrictionSecurityPlugin);
                    enableIpRestriction = true;
                    return;
                }
            }

        } else {
        }
        enableIpLimitation.setValue(false);
        setVisibleIpLimit(false);
    }


    private void setVisibleIpLimit(boolean show) {
        vpIpAllow.setVisible(show);
        vpIpDeny.setVisible(show);
    }

    @Subscribe("enableIpLimitation")
    public void onEnableIpLimitationValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (enableIpLimitation.isChecked()) {
            setVisibleIpLimit(true);
        } else {
            setVisibleIpLimit(false);
        }
    }


    private ArrayList<String> vpToLst(ValuesPicker<String> vp) {
        return vp.getValue() == null ? new ArrayList<String>() : new ArrayList<String>(vp.getValue().stream().toList());
    }

    private IpRestrictionSecurityPlugin getIpLimitationFromUI() {
        try {
            IpRestrictionSecurityPluginConfig config = new IpRestrictionSecurityPluginConfig(vpToLst(vpIpAllow), vpToLst(vpIpDeny));
            IpRestrictionSecurityPlugin plugin = new IpRestrictionSecurityPlugin(config, UUID.fromString(getEditedEntity().getKongServiceId().toString()));
            return plugin;
        } catch (Exception e) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Couldn't Get IP Limitation: " + e.getMessage())
                    .show();
        }
        return null;
    }

    private void setIpLimitationForUI(IpRestrictionSecurityPlugin plugin) {
        try {
            IpRestrictionSecurityPluginConfig config = plugin.getConfig();
            vpIpAllow.setValue(config.getAllow() == null ? null : config.getAllow());
            vpIpDeny.setValue(config.getDeny() == null ? null : config.getDeny());
        } catch (Exception e) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Couldn't Set IP Limitation For UI: " + e.getMessage())
                    .show();
        }
    }

    private void createOrUpdateIpRestriction(IpRestrictionSecurityPlugin plugin) {
        StringBuilder pluginId = new StringBuilder();
        HandledError handledError = new HandledError();
        try {
            kongConnector.createOrUpdateIpRestriction(getEditedEntity().getKongServiceId().toString(), plugin, pluginId, handledError);
        } catch (Exception e) {
            notifications.create(Notifications.NotificationType.ERROR).withCaption(e.getMessage()).show();
        }
    }

    private IpRestrictionSecurityPlugin getIpRestrictionFromDB(String _plugins) {
        JsonObject convertedObject = new Gson().fromJson(_plugins, JsonObject.class);
        JsonArray jsonArray = convertedObject.get("data").getAsJsonArray();
        if (jsonArray.size() == 0) {
            return null;
        } else {
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject obj = jsonArray.get(i).getAsJsonObject();
                if (obj.get("name").getAsString().equals("ip-restriction")) {
                    obj.remove("consumer");
                    String content = obj.toString();
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                        IpRestrictionSecurityPlugin plugin = objectMapper.readValue(content, IpRestrictionSecurityPlugin.class);
                        return plugin;
                    } catch (Exception e) {
                        handledError.setErrorMsg(e.getMessage());
                    }
                }
            }
            return null;
        }

    }

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        int ret = 0;
        List<String> allow = vpToLst(vpIpAllow);
        List<String> deny = vpToLst(vpIpDeny);
        if (enableIpLimitation.isChecked() == false) {
            if (enableIpRestriction == true) {
                //delete or disable
                ret = kongConnector.deletePluginWithType(getEditedEntity().getName(), "ip-restriction", handledError);
            }
        } else {
            if (allow.isEmpty() && deny.isEmpty()) {
                //delete or disable
                ret = kongConnector.deletePluginWithType(getEditedEntity().getName(), "ip-restriction", handledError);
            } else {
                if (checkValid(event, allow, deny)) {
                    createOrUpdateIpRestriction(getIpLimitationFromUI());
                } else {
                    return;
                }
            }
        }
        if (ret == -1) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(handledError.getErrorMsg()).show();
            event.preventCommit();
        }
        getEditedEntity().setPlugin(kongConnector.getRawServicePlugins(getEditedEntity().getName(), handledError));
    }

    private static Pattern pValue = Pattern.compile("((^\\s*((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))\\s*$)|(^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$))");

    private boolean checkValid(BeforeCommitChangesEvent event, List<String> allow, List<String> deny) {
        List<ValuesPicker> checkPValueList = new ArrayList<ValuesPicker>(Arrays.asList(vpIpAllow, vpIpDeny));
        Set<String> ipSet = new HashSet<String>(allow);
        ipSet.addAll(deny);

        for (ValuesPicker ele : checkPValueList) {
            if (checkAndNotification(ele, pValue)) {
                event.preventCommit();
                return false;
            }
        }
        if (allow.size() + deny.size() != ipSet.size()) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Check IP List, Maybe the IP is duplicated")
                    .show();
            event.preventCommit();
            return false;
        }
        return true;
    }

    private boolean checkAndNotification(ValuesPicker<String> vp, Pattern p) {
        if (vp.getValue() == null) {
            return false;
        }
        if (!vp.getValue().stream().allMatch(st -> p.matcher(st).matches())) {
            notifications.create(Notifications.NotificationType.WARNING).
                    withCaption("Check " + vp.getCaption() + " List")
                    .show();
            vp.addStyleName("v-label-failure");
            return true;
        }
        vp.removeStyleName("v-label-failure");
        return false;
    }
}
