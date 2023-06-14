package com.wtv.webcastmanagement.repository;

import com.wtv.webcastmanagement.entity.zoom.Meeting;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MeetingRepository extends MongoRepository<Meeting,String> {
    Meeting findMeetingByWebcastId(String webcastId);
}
