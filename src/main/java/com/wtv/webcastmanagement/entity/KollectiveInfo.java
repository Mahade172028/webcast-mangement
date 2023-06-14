package com.wtv.webcastmanagement.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "kollectiveInfo")
public class KollectiveInfo {
    @Id
    private String id;
    private String moid;
    private String url;
    private String tenantId;
    private String title;
    private String contentToken;
    private String kollectiveUrl;
}
