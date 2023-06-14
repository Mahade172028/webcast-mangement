package com.wtv.webcastmanagement.entity.Kollective;

import lombok.Data;

import java.util.List;

@Data
public class Items {
    private String title;
    private String description;
    private List<Source> sources;
}
