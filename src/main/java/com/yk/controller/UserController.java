package com.yk.controller;


import com.spring.annotation.YKAutowried;
import com.spring.annotation.YKController;
import com.spring.annotation.YKRequestMapping;
import com.spring.annotation.YKResponseBody;
import com.yk.bean.User;
import com.yk.service.UserService;

import java.util.List;


@YKController
@YKRequestMapping("/user")
public class UserController {

    @YKAutowried
    private UserService userService;

    @YKResponseBody
    @YKRequestMapping("/getAllUser")
    public List<User> getAllUser(){
        List<User> user = null;
        user = userService.getAllUser();
        return user;
    }

}
