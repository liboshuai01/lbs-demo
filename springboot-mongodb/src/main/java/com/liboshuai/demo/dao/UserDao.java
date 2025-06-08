package com.liboshuai.demo.dao;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "user")
public class UserDao {
    @Id
    private String id;

    private String name;

    private Integer age;

    private Boolean gender;
}
