package com.yk.service.impl;

import com.spring.annotation.YKAutowried;
import com.spring.annotation.YKService;
import com.yk.bean.User;
import com.yk.dao.UserDao;
import com.yk.service.UserService;

import java.util.List;
@YKService
public class UserServiceImpl implements UserService {

    @YKAutowried
    private UserDao userDao;

    public List<User> getAllUser() {
        return userDao.getAll();
    }
}
