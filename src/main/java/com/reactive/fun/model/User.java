package com.reactive.fun.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class User {

    @Id
    private int id;
    private String name;

    public User(int i, String s) {
        id = i;
        name = s;

    }
}
