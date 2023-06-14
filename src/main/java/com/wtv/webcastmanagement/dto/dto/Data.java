
package com.wtv.webcastmanagement.dto.dto;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@Generated("jsonschema2pojo")
public class Data {

    private Object mime;
    private Object encodingInfo;
    private String playType;
    private String cacheId;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Object getMime() {
        return mime;
    }

    public void setMime(Object mime) {
        this.mime = mime;
    }

    public Object getEncodingInfo() {
        return encodingInfo;
    }

    public void setEncodingInfo(Object encodingInfo) {
        this.encodingInfo = encodingInfo;
    }

    public String getPlayType() {
        return playType;
    }

    public void setPlayType(String playType) {
        this.playType = playType;
    }

    public String getCacheId() {
        return cacheId;
    }

    public void setCacheId(String cacheId) {
        this.cacheId = cacheId;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
