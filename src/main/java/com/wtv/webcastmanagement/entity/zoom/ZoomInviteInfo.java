package com.wtv.webcastmanagement.entity.zoom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomInviteInfo {
    public String invitation;
    public List<String> sip_links;
}
