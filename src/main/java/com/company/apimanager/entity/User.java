package com.company.apimanager.entity;

import com.company.apimanager.app.EncodePassword;
import com.company.apimanager.app.RestClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.jmix.core.HasTimeZone;
import io.jmix.core.annotation.Secret;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.security.authentication.JmixUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.*;

@JmixEntity
@Entity
@Table(name = "USER_", indexes = {
        @Index(name = "IDX_USER__ON_USERNAME", columnList = "USERNAME", unique = true),
        @Index(name = "IDX_USER__PRICING_PLAN", columnList = "PRICING_PLAN")
})
public class User implements JmixUserDetails, HasTimeZone {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    private UUID id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "USERNAME", nullable = false)
    protected String username;

    @Secret
    @SystemLevel
    @Column(name = "PASSWORD")
    protected String password;

    @Column(name = "FIRST_NAME")
    protected String firstName;

    @Column(name = "LAST_NAME")
    protected String lastName;

    @Email
    @Column(name = "EMAIL")
    protected String email;

    @Column(name = "ACTIVE")
    protected Boolean active = true;

    @Column(name = "TIME_ZONE_ID")
    protected String timeZoneId;

    //Additional: TuanNA6 - 08/06/2022
    @Column(name = "PROVIDER_PLAN")
    protected String providerPlan;

    @JoinColumn(name = "PRICING_PLAN")
    @ManyToOne(fetch = FetchType.LAZY)
    private PricingPlan pricingPlan;

    @Column(name = "TENANT_NAME")
    protected String tenantName;

    @Transient
    protected Collection<? extends GrantedAuthority> authorities;

    public PricingPlan getPricingPlan() {
        return pricingPlan;
    }

    public void setPricingPlan(PricingPlan pricingPlan) {
        this.pricingPlan = pricingPlan;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities != null ? authorities : Collections.emptyList();
    }

    @Override
    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(active);
    }

    @InstanceName
    @DependsOnProperties({"firstName", "lastName", "username"})
    public String getDisplayName() {
        return String.format("%s %s [%s]", (firstName != null ? firstName : ""),
                (lastName != null ? lastName : ""), username).trim();
    }

    @Override
    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public String getProviderPlan() {
        return providerPlan;
    }

    public void setProviderPlan(String providerPlan) {
        this.providerPlan = providerPlan;
    }

    public String getTenantName() { return tenantName; }

    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public static List<String> getAvailableProviderPlans() {
        List<String> list = new ArrayList<>();
        list.add("Pilot");
        list.add("Normal");
        list.add("Enterprise");
        return list;
    }

    public static JsonArray checkUser (String baseUrl, String tenantName, String accessToken) {
        RestClient restTemplate = new RestClient();
        String checkUserUrl = baseUrl + "/rest/entities/User/search";
        String filterQuery = "{\n" +
                "  \"filter\": {\n" +
                "    \"conditions\": [\n" +
                "      {\n" +
                "        \"property\": \"tenantName\",\n" +
                "        \"operator\": \"=\",\n" +
                "        \"value\": \"" + tenantName + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        String checkResult = restTemplate.postToken(checkUserUrl, filterQuery, "Authorization", "Bearer " + accessToken);
        return new Gson().fromJson(checkResult, JsonArray.class);
    }

    public static String createUser (String baseUrl, String username, String plan, String tenantName, String pricingId, String accessToken) {
        RestClient restTemplate = new RestClient();
        PasswordEncoder encoder = EncodePassword.setPasswordEncoder();
        String newPass = encoder.encode(username);
        String postStr = "{\n" +
                "        \"_entityName\": \"User\",\n" +
                "        \"_instanceName\": \"SSI Company [ssi]\",\n" +
                "        \"firstName\": \"" + username + "\",\n" +
                "        \"lastName\": \"Company\",\n" +
                "        \"timeZoneId\": \"Asia/Bangkok\",\n" +
                "        \"active\": true,\n" +
                "        \"providerPlan\": \"" + plan + "\",\n" +
                "        \"pricingPlan\": {\"id\": \"" + pricingId + "\"},\n" +
                "        \"tenantName\": \"" + tenantName + "\",\n" +
                "        \"username\": \"" + username + "\",\n" +
                "        \"password\": \"" + newPass + "\"\n" +
                "    }";
        String createUserUrl = baseUrl + "/rest/entities/User";
        String createResult = restTemplate.postToken(createUserUrl, postStr, "Authorization", "Bearer " + accessToken);
        String roleBody = "{\n" +
                "    \"username\": \"" + username + "\",\n" +
                "    \"roleCode\": \"api_provider_owner\",\n" +
                "    \"roleType\": \"resource\"\n" +
                "}";
        String createRoleUrl = baseUrl + "/rest/entities/sec_RoleAssignmentEntity";
        restTemplate.postToken(createRoleUrl, roleBody, "Authorization", "Bearer " + accessToken);
        JsonObject userData = new Gson().fromJson(createResult, JsonObject.class);
        return userData.get("id").getAsString();
    }
}