package com.wtv.webcastmanagement.dto.dto.others;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileConverterResponse {
    String msg;
    List<String> images;
    String numOfImages;
}
