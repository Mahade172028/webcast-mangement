package com.wtv.webcastmanagement.dto.dto.primaryMark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Container{
    public String id;
    public Text text;
    public boolean hideLive;
}
