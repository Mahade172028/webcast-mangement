package com.wtv.webcastmanagement.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "webcasts")
public class Webcast {
    @Id
    private String id;
    private String timeZone;
    private Integer webcastId;
    private String webcastType;
    private Integer invitedGuest;
    private String regRequired;
    private String type;
    private String duration;
    private String bitRate;
    private boolean enableChinaCDN;
    private boolean isECDNActive;
    private String p2pVendor;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;

}
