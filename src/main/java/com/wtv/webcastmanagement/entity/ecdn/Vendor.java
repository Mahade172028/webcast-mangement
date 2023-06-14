package com.wtv.webcastmanagement.entity.ecdn;

import lombok.Data;

import java.util.List;

@Data
public class Vendor {
    String id;
    String name;
    List<Secret> secrets;
}
