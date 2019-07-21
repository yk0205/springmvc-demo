package com.yk.service.impl;

import com.spring.annotation.MyAutowried;
import com.spring.annotation.MyService;
import com.yk.bean.User;
import com.yk.dao.UserDao;
import com.yk.service.UserService;
import java.util.List;

@MyService
public class UserServiceImpl implements UserService {

    @MyAutowried
    private UserDao userDao;

    public List<User> getAllUser() {
        return userDao.getAll();
    }

    @Override
    public User getUserById(int id) {
        return userDao.selectById(id);
    }
}
