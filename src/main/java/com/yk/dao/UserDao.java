package com.yk.dao;

import com.spring.annotation.MyRepository;
import com.yk.bean.User;

import java.util.ArrayList;
import java.util.List;


@MyRepository
public class UserDao {

    private List<User> list = null;

    public List<User> getAll() {
        list = new ArrayList<>();
        list.add(new User(1, "d"));
        list.add(new User(2, "a"));
        list.add(new User(3, "b"));
        list.add(new User(4, "c"));
        return list;
    }

    public User selectById(int id) {
        return new User(1, "d");
    }
}
