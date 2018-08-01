package com.reactive.fun.repository;

import com.reactive.fun.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMongoRepository extends ReactiveMongoRepository<User,String>{
}
