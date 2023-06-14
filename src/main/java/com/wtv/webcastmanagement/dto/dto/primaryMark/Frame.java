package com.wtv.webcastmanagement.dto.dto.primaryMark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Frame{
    public String type;
    public String frameId;
    public String view;
    public Title title;
    public Body body;
    public String url;
    public String refId;
    public String imageType;
    public Object link;
    public Object specialEffect;
    public ArrayList<Container> containers;
}
