
package com.wtv.webcastmanagement.dto.dto;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Generated("jsonschema2pojo")
public class Config {

    private String app;
    private String tenantId;
    private String sdkUrl;
    private String ah;
    private String ch;
    private String th;
    private String sgh;
    private String sth;
    private Playback playback;
    private Peering peering;
    private Report report;
    private Agent agent;
    private Legacy legacy;
    private List<String> labels = null;
    private List<String> localities = null;
    private String b;
    private String t;
    private String r;
    private String a;
    private int p;
    private int tvd;
    private boolean wel;
    private boolean wpl;
    private int tle;
    private boolean voo;
    private int iar;
    private int peeringVersion;
    private boolean tpe;
    private boolean wev;
    private boolean wpv;
    private boolean svt;
    private int tas;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSdkUrl() {
        return sdkUrl;
    }

    public void setSdkUrl(String sdkUrl) {
        this.sdkUrl = sdkUrl;
    }

    public String getAh() {
        return ah;
    }

    public void setAh(String ah) {
        this.ah = ah;
    }

    public String getCh() {
        return ch;
    }

    public void setCh(String ch) {
        this.ch = ch;
    }

    public String getTh() {
        return th;
    }

    public void setTh(String th) {
        this.th = th;
    }

    public String getSgh() {
        return sgh;
    }

    public void setSgh(String sgh) {
        this.sgh = sgh;
    }

    public String getSth() {
        return sth;
    }

    public void setSth(String sth) {
        this.sth = sth;
    }

    public Playback getPlayback() {
        return playback;
    }

    public void setPlayback(Playback playback) {
        this.playback = playback;
    }

    public Peering getPeering() {
        return peering;
    }

    public void setPeering(Peering peering) {
        this.peering = peering;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public Legacy getLegacy() {
        return legacy;
    }

    public void setLegacy(Legacy legacy) {
        this.legacy = legacy;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<String> getLocalities() {
        return localities;
    }

    public void setLocalities(List<String> localities) {
        this.localities = localities;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public int getP() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
    }

    public int getTvd() {
        return tvd;
    }

    public void setTvd(int tvd) {
        this.tvd = tvd;
    }

    public boolean isWel() {
        return wel;
    }

    public void setWel(boolean wel) {
        this.wel = wel;
    }

    public boolean isWpl() {
        return wpl;
    }

    public void setWpl(boolean wpl) {
        this.wpl = wpl;
    }

    public int getTle() {
        return tle;
    }

    public void setTle(int tle) {
        this.tle = tle;
    }

    public boolean isVoo() {
        return voo;
    }

    public void setVoo(boolean voo) {
        this.voo = voo;
    }

    public int getIar() {
        return iar;
    }

    public void setIar(int iar) {
        this.iar = iar;
    }

    public int getPeeringVersion() {
        return peeringVersion;
    }

    public void setPeeringVersion(int peeringVersion) {
        this.peeringVersion = peeringVersion;
    }

    public boolean isTpe() {
        return tpe;
    }

    public void setTpe(boolean tpe) {
        this.tpe = tpe;
    }

    public boolean isWev() {
        return wev;
    }

    public void setWev(boolean wev) {
        this.wev = wev;
    }

    public boolean isWpv() {
        return wpv;
    }

    public void setWpv(boolean wpv) {
        this.wpv = wpv;
    }

    public boolean isSvt() {
        return svt;
    }

    public void setSvt(boolean svt) {
        this.svt = svt;
    }

    public int getTas() {
        return tas;
    }

    public void setTas(int tas) {
        this.tas = tas;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
