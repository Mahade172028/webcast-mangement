package com.wtv.webcastmanagement.entity.legacy;
import lombok.Data;
import java.util.List;
@Data
public class LegacyBroadcast {
    private String coName;
    private String csoName;
    private boolean lowLatency;
    private Integer id;
    private Integer cso;
    private Integer co;
    private String status;
    private String name;
    private String createDate;
    private String scheduleLiveStartDate;
    private String ondemandOverDate;
    private Integer ondemandLength;
    private String defaultLanguage;
    private String country;
    List<String> additionalLanguages ;
    List<String> features ;
    List<String> metaInfo ;
}
