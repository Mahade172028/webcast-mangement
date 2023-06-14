package com.wtv.webcastmanagement.dto.dto.FileResponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payload{
    public int count;
    public Data data;
}
