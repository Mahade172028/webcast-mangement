package com.wtv.webcastmanagement.entity.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SlideResponse {

    private String lang;
    @JsonProperty("isDefaultLang")
    private boolean isDefaultLang;
    private List<SlideData> data;

}
