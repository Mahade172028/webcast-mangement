package com.wtv.webcastmanagement.entity.zoom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@ToString
@Document(collection = "meetings")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Meeting {
    @Id
    private String _id;
    private String uuid;
    private Long id;
    private String host_id;
    private String topic;
    private Integer type;
    private String status;
    private String start_time;
    private Integer duration;
    private String timezone;
    private String created_at;
    private String start_url;
    private String join_url;
    private Settings settings;
    private String webcastId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
