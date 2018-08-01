package com.reactive.fun.service;

import com.reactive.fun.model.User;
import com.reactive.fun.repository.UserMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
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


    public Flux<User> getAll() {
        Flux<User> usersFromMongo = getUsersFromMongo().share().publish().autoConnect(2);

        Long l = System.currentTimeMillis();
        Disposable subscribe = usersFromMongo
                .buffer(1)
                .flatMap(users -> Flux.defer(() -> addUsersToCache(users)).subscribeOn(Schedulers.elastic()))
                .doOnComplete(() -> System.out.println(System.currentTimeMillis() - l))
                .subscribe();



        return getUsersFromCache()
                .switchIfEmpty(usersFromMongo);
    }

    private Mono<Long> addUsersToCache(List<User> userList) {
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
