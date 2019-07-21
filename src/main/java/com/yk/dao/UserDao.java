package com.yk.dao;

import com.spring.annotation.MyRepository;
import com.yk.bean.User;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@MyRepository
public class UserDao {

    private List<User> list = new ArrayList<>();

    @PostConstruct
    public void init() {
        list.add(new User(1, "d"));
        list.add(new User(2, "a"));
        list.add(new User(3, "b"));
        list.add(new User(4, "c"));
    }

    public List<User> getAll() {
        return list;
    }

    public User selectById(int id) {
        List<User> collect = list.stream()
                .filter(user -> user.getId() == id)
                .collect(Collectors.toList());
        return collect.get(0);
    }
}
