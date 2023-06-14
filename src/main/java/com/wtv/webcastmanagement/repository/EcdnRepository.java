package com.wtv.webcastmanagement.repository;

import com.wtv.webcastmanagement.entity.ecdn.Ecdn;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface EcdnRepository extends MongoRepository<Ecdn , String> {
    @Query("{'entityId': ?0}")
    Ecdn findByEntityId(Integer id);

    @Query("{$or: [{'entityId': ?0},{'entityId': ?1},{'entityId': ?2}]}")
    Optional<Ecdn> findEcdnByWebcastIdOrCsoOrCo(Integer co , Integer cso , Integer webcastId);
}
