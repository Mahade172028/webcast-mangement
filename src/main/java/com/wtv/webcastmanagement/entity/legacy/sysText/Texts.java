package com.wtv.webcastmanagement.entity.legacy.sysText;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Texts{
    String lang;
    String text;
}
