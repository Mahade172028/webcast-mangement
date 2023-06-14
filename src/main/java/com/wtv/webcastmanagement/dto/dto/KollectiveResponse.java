
package com.wtv.webcastmanagement.dto.dto;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Generated("jsonschema2pojo")
public class KollectiveResponse {

    private String token;
    private String contentToken;
    private List<Item> items = null;
    private Config config;
    private Legacy__1 legacy;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getContentToken() {
        return contentToken;
    }

    public void setContentToken(String contentToken) {
        this.contentToken = contentToken;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public Legacy__1 getLegacy() {
        return legacy;
    }

    public void setLegacy(Legacy__1 legacy) {
        this.legacy = legacy;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
