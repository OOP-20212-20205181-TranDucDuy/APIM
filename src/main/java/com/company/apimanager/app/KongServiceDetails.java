package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class KongServiceDetails {
    private long write_timeout;
    private String path;
    private String name;
    private long read_timeout;
    private String id;
    private String host;
    private int retries;
    private long connect_timeout;
    private long updated_at;
    private long created_at;
    private boolean enabled;
    private String protocol;
    private long port;

    public KongServiceDetails(long write_timeout, String path, String name, long read_timeout,
                            String id, String host, int retries, long connect_timeout, long updated_at
                            , long created_at, boolean enabled, String protocol, long port) {
        this.write_timeout = write_timeout;
        this.path = path;
        this.name = name;
        this.read_timeout = read_timeout;
        this.id = id;
        this.host = host;
        this.retries = retries;
        this.connect_timeout = connect_timeout;
        this.updated_at = updated_at;
        this.created_at = created_at;
        this.enabled = enabled;
        this.protocol = protocol;
        this.port = port;
    }
    public KongServiceDetails() {

    }
    public void Assign(KongServiceDetails other) {
        this.write_timeout = other.write_timeout;
        this.path = other.path;
        this.name = other.name;
        this.read_timeout = other.read_timeout;
        this.id = other.id;
        this.host = other.host;
        this.retries = other.retries;
        this.connect_timeout = other.connect_timeout;
        this.updated_at = other.updated_at;
        this.created_at = other.created_at;
        this.enabled = other.enabled;
        this.protocol = other.protocol;
        this.port = other.port;
    }
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public long getPort() {
        return port;
    }

    public int getRetries() {
        return retries;
    }

    public long getConnect_timeout() {
        return connect_timeout;
    }

    public long getRead_timeout() {
        return read_timeout;
    }

    public long getWrite_timeout() {
        return write_timeout;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public long getCreated_at() {
        return created_at;
    }

    public String getProtocol() {
        return protocol;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    public void setConnect_timeout(long connect_timeout) {
        this.connect_timeout = connect_timeout;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setPort(long port) {
        this.port = port;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setRead_timeout(long read_timeout) {
        this.read_timeout = read_timeout;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }

    public void setWrite_timeout(long write_timeout) {
        this.write_timeout = write_timeout;
    }
}