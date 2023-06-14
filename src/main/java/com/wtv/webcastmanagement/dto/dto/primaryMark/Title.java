package com.wtv.webcastmanagement.dto.dto.primaryMark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Title{
    public String type;
    public Object text;
}
