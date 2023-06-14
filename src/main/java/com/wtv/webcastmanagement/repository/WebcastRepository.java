package com.wtv.webcastmanagement.repository;

import com.wtv.webcastmanagement.entity.Webcast;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface WebcastRepository extends MongoRepository<Webcast , String> {

    @Query("{'webcastId' : ?0}")
    Webcast findByWebcastId(Integer webcastId);
}
