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
    public User getUserById(HttpServletRequest request, HttpServletResponse response,
                            @MyRequestParam("id") int id   ) {

        User user = null;
        try {
            user = userService.getUserById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;

    }
    @MyResponseBody
    @MyRequestMapping("/query")
    public String query(HttpServletRequest request, HttpServletResponse response,
                            @MyRequestParam("id") Integer id,
                            @MyRequestParam("name") String name ) {

        String str = null;
        try {
            str = userService.query(id,name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;

    }

}
