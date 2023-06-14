
package com.wtv.webcastmanagement.dto.dto;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@Generated("jsonschema2pojo")
public class Webrtc {

    private boolean enableDscpTagging;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public boolean isEnableDscpTagging() {
        return enableDscpTagging;
    }

    public void setEnableDscpTagging(boolean enableDscpTagging) {
        this.enableDscpTagging = enableDscpTagging;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
