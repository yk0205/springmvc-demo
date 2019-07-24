package com.yk.controller;


import com.spring.annotation.*;
import com.yk.bean.User;
import com.yk.service.UserService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@MyController
@MyRequestMapping("/user")
public class UserController {

    @MyAutowired
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
    public User getUserById(HttpServletRequest request,
                            HttpServletResponse response,
                            @MyRequestParam("param") String param ,
                            @MyRequestParam("id") Integer id
    ) {

        User user = null;
        try {
            user = userService.getUserById(Integer.valueOf(param));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;

    }

}
