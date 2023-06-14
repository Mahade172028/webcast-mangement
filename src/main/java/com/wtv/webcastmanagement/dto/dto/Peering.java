
package com.wtv.webcastmanagement.dto.dto;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@Generated("jsonschema2pojo")
public class Peering {

    private int version;
    private boolean pullThroughCache;
    private boolean proxyIngest;
    private int peerFetchTimeout;
    private int maxClusterDepth;
    private Cache cache;
    private Webrtc webrtc;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isPullThroughCache() {
        return pullThroughCache;
    }

    public void setPullThroughCache(boolean pullThroughCache) {
        this.pullThroughCache = pullThroughCache;
    }

    public boolean isProxyIngest() {
        return proxyIngest;
    }

    public void setProxyIngest(boolean proxyIngest) {
        this.proxyIngest = proxyIngest;
    }

    public int getPeerFetchTimeout() {
        return peerFetchTimeout;
    }

    public void setPeerFetchTimeout(int peerFetchTimeout) {
        this.peerFetchTimeout = peerFetchTimeout;
    }

    public int getMaxClusterDepth() {
        return maxClusterDepth;
    }

    public void setMaxClusterDepth(int maxClusterDepth) {
        this.maxClusterDepth = maxClusterDepth;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public Webrtc getWebrtc() {
        return webrtc;
    }

    public void setWebrtc(Webrtc webrtc) {
        this.webrtc = webrtc;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
