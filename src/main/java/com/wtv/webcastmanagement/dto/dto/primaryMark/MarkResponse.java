package com.wtv.webcastmanagement.dto.dto.primaryMark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarkResponse {
    public String type;
    public String id;
    public String uniqueId;
    public String name;
    public String comment;
    public int time;
    public Object index;
    public ArrayList<Frame> frame;
}
