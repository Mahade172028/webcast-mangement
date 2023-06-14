
package com.wtv.webcastmanagement.dto.dto;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@Generated("jsonschema2pojo")
public class Live {

    private String priority;
    private boolean blackout;
    private int delay;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isBlackout() {
        return blackout;
    }

    public void setBlackout(boolean blackout) {
        this.blackout = blackout;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
