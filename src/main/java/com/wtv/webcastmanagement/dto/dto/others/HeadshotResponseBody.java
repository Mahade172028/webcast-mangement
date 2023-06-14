package com.wtv.webcastmanagement.dto.dto.others;

import com.wtv.webcastmanagement.entity.legacy.SlideResponse;
import lombok.Data;

import java.util.List;

@Data
public class HeadshotResponseBody {
    String primaryHeadshot;
    List<SlideResponse> slideResponseBody;
}
