package com.wtv.webcastmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wtv.webcastmanagement.entity.legacy.StreamInfo;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebcastPayload {
    private String timeZone;
    private Integer webcastId;
    private String webcastType;
    private Integer invitedGuest;
    private String regRequired;
    private String type;
    private String duration;
    private String bitRate;
    private boolean enableChinaCDN;
    private boolean currentChinaCdnStatus;
    private boolean enableECDNActive;
    private boolean currentECDNStatus;
    private Boolean changeWebcastType;
    private String p2pVendor;
    private String title;
    private Integer cso;
    private Integer co;
    private String scheduleLiveStartDate;
    private String ondemandOverDate;
    private String defaultLanguage;
    private String currentDefaultLanguage;
    private List<String> additionalLanguage;
    private List<String> currentAdditionalLanguage;
    private List<String> features;
    private String status;
    private List<StreamInfo> previewStreamList;
    private List<StreamInfo> ondemandStreamsList;
    private String meeting;
}
