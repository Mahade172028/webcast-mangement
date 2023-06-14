package com.wtv.webcastmanagement.dto.dto.FileResponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Data{
    public String filename;
    public int orifinalSize;
}
