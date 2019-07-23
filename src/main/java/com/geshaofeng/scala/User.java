package com.geshaofeng.scala;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class User {
    int age;
    String name;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public static List<User> getUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User("Gary", 18));
        users.add(new User("Tom", 19));
        users.add(new User("Peter", 17));
        users.add(new User("Mark", 20));
        HashSet<User> users1 = new HashSet<>(users);
        return users;

    }

}
