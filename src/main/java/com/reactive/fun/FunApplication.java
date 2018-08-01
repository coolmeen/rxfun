package com.reactive.fun;

import com.reactive.fun.model.User;
import com.reactive.fun.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

@RestController
@SpringBootApplication
public class FunApplication {

    public static void main(String[] args) {
        SpringApplication.run(FunApplication.class, args);
    }


    @Autowired
    UserService userService;

    @RequestMapping(value = "/getAll",produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<User> getAllDetails() {
        return userService.getAll().onBackpressureBuffer(1000);
    }



    @RequestMapping(value = "/getAllTogether")
    public Flux<User> getAllDetailsTO() {
        return userService.getAll();
    }



    @RequestMapping(value = "/getCache",produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<User> getAllCache() {
        return userService.getAllFromCache().delayElements(Duration.ofNanos(10));
    }


}
