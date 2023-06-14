package com.wtv.webcastmanagement.entity.zoom;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class MeetingResponse {
    public String page_size;
    public List<Meeting> meetings;
}
