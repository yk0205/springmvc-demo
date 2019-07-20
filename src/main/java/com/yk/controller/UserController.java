package com.yk.controller;


import com.spring.annotation.MyAutowried;
import com.spring.annotation.MyController;
import com.spring.annotation.MyRequestMapping;
import com.spring.annotation.MyResponseBody;
import com.yk.bean.User;
import com.yk.service.UserService;


import java.util.List;


@MyController
@MyRequestMapping("/user")
public class UserController {

    @MyAutowried
    private UserService userService;

    @MyResponseBody
    @MyRequestMapping("/getAllUser")
    public List<User> getAllUser(){
        List<User> user = null;
        user = userService.getAllUser();
        return user;
    }

}
