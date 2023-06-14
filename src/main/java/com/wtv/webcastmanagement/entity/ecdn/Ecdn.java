package com.wtv.webcastmanagement.entity.ecdn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "ecdns")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ecdn {
    @Id
    private String id;
    private Integer entityId;
    private Integer parentId;
    private List<Vendor> vendor = new ArrayList<>();
    private String cascadeLevel;

}
