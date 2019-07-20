package com.yk.service;


import com.spring.annotation.YKAutowried;
import com.spring.annotation.YKService;
import com.yk.bean.User;
import com.yk.dao.UserDao;

import java.util.List;

@YKService
public class UserService {

    @YKAutowried
    private UserDao userDao;

    public List<User> getAllUser() {
        return userDao.getAll();
    }
}
