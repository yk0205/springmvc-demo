package com.yk.dao;

import com.spring.annotation.MyRepository;
import com.yk.bean.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@MyRepository
public class UserDao {

    private static List<User> list = new ArrayList<>();


    public UserDao() {
        list.add(new User(1, "d"));
        list.add(new User(2, "a"));
        list.add(new User(3, "b"));
        list.add(new User(4, "c"));
        list.add(new User(28, "c"));
    }

    public List<User> getAll() {
        return list;
    }

    public User selectById(int id) {
        Optional<User> first = list.stream().filter(user -> user.getId() == id).findFirst();
        User user = first.get();
        return user;
    }
}
