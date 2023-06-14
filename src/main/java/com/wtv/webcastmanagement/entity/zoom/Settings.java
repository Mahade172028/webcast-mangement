package com.wtv.webcastmanagement.entity.zoom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings {
    String auto_recording;
    boolean join_before_host;
}
