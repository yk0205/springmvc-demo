package com.yk.service;


import com.yk.bean.User;

import java.util.List;

public interface UserService {

    List<User> getAllUser();

    User getUserById(int id);

    String query(Integer id, String name,Long department);

}
