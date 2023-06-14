
package com.wtv.webcastmanagement.dto.dto;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@Generated("jsonschema2pojo")
public class Legacy {

    private boolean disabled;
    private boolean usePreRoll;
    private int timeoutAgentStreaming;
    private int timeoutAgentStreamingLive;
    private int timeoutAgentStreamingVod;
    private boolean vodOptOut;
    private boolean briskOnlyReporting;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isUsePreRoll() {
        return usePreRoll;
    }

    public void setUsePreRoll(boolean usePreRoll) {
        this.usePreRoll = usePreRoll;
    }

    public int getTimeoutAgentStreaming() {
        return timeoutAgentStreaming;
    }

    public void setTimeoutAgentStreaming(int timeoutAgentStreaming) {
        this.timeoutAgentStreaming = timeoutAgentStreaming;
    }

    public int getTimeoutAgentStreamingLive() {
        return timeoutAgentStreamingLive;
    }

    public void setTimeoutAgentStreamingLive(int timeoutAgentStreamingLive) {
        this.timeoutAgentStreamingLive = timeoutAgentStreamingLive;
    }

    public int getTimeoutAgentStreamingVod() {
        return timeoutAgentStreamingVod;
    }

    public void setTimeoutAgentStreamingVod(int timeoutAgentStreamingVod) {
        this.timeoutAgentStreamingVod = timeoutAgentStreamingVod;
    }

    public boolean isVodOptOut() {
        return vodOptOut;
    }

    public void setVodOptOut(boolean vodOptOut) {
        this.vodOptOut = vodOptOut;
    }

    public boolean isBriskOnlyReporting() {
        return briskOnlyReporting;
    }

    public void setBriskOnlyReporting(boolean briskOnlyReporting) {
        this.briskOnlyReporting = briskOnlyReporting;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
