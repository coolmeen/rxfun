package com.reactive.fun;

import com.reactive.fun.model.User;
import com.reactive.fun.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class lineRunner implements CommandLineRunner {
    @Autowired
    UserService userService;

        @Override
        public void run(String... args) throws Exception {
            List<User> userList = new LinkedList<>();
            for(int i = 0;i<100000;i++) {
                userList.add(new User(i,RandomStringUtils.randomAlphabetic(10)));
            }
            userService.insertData(userList);
        }
    }
