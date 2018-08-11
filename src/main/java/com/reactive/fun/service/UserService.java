package com.reactive.fun.service;

import com.reactive.fun.model.User;
import com.reactive.fun.repository.UserMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
public class UserService {


    private UserMongoRepository userMongoRepository;

    private ReactiveRedisConnectionFactory connectionFactory;


    private ReactiveRedisTemplate<String,User> reactiveRedisOperations;

    @Autowired
    public UserService(ReactiveRedisConnectionFactory connectionFactory, UserMongoRepository userMongoRepository, ReactiveRedisTemplate<String, User> reactiveRedisOperations) {
        this.connectionFactory = connectionFactory;
        this.userMongoRepository = userMongoRepository;
        this.reactiveRedisOperations = reactiveRedisOperations;
    }


    //incase of a cache miss trying to create publisher with two subscribers(redis and user)
    public Flux<User> getAll() {
        Flux<User> mongoFlux = getUsersFromMongo().publish().autoConnect(2);

        insetToRedisOnCacheMiss(mongoFlux);


        return getUsersFromCache()
                .switchIfEmpty(mongoFlux);
    }

    private void insetToRedisOnCacheMiss(Flux<User> usersFromMongo) {
        Long l = System.currentTimeMillis();
        usersFromMongo.subscribeOn(Schedulers.elastic())
                .buffer(1000)
                .doOnNext(userFlux -> System.out.println("redis subscriber" + Thread.currentThread()))
                .flatMapSequential(users -> addUsersToCache(users),40)
                .subscribe();
    }

    private Mono<Long> addUsersToCache(List<User> userList) {
        System.out.println("added 1000 elements to redis");
        return reactiveRedisOperations
                .opsForSet()
                .add("users", userList.toArray(new User[userList.size()]));
    }

    private Flux<User> getUsersFromMongo() {
         return userMongoRepository.findAll();
    }

    private Flux<User> getUsersFromCache() {
        return reactiveRedisOperations.opsForSet().members("users");
    }

    public void insertData(List<User> userList) {
        List<User> user = userMongoRepository.saveAll(userList).collectList().block() ;
        System.out.println(user.size());

    }

    public Flux<User> getAllFromCache() {
        return getUsersFromCache();
    }
}
