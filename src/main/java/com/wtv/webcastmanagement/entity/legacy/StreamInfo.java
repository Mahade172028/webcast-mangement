package com.wtv.webcastmanagement.entity.legacy;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class StreamInfo implements Cloneable{
    public String format;
    public String media;
    public Integer quality;
    public String language;
    public String locale;
    public String encoder;
    public String protocol;
    public String framesize;
    public String startTime;
    public String endTime;
    public String url;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
