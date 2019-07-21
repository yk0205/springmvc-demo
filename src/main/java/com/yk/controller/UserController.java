package com.yk.controller;


import com.spring.annotation.*;
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
    public List<User> getAllUser() {

        List<User> userList = null;

        try {
            userList = userService.getAllUser();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList;
    }


    @MyResponseBody
    @MyRequestMapping("/getUserById")
    public User getUserById(@MyRequestParam(value = "id") int id) {

        User user = null;
        try {
            user = userService.getUserById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

}
