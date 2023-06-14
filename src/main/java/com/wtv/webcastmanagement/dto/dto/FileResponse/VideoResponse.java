package com.wtv.webcastmanagement.dto.dto.FileResponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoResponse {
    public long nonce;
    public int status;
    public String message;
    public Payload payload;
}
