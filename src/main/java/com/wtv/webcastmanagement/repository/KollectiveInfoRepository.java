package com.wtv.webcastmanagement.repository;

import com.wtv.webcastmanagement.entity.KollectiveInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KollectiveInfoRepository extends MongoRepository<KollectiveInfo , String> {
}
