package com.bookbrew.customer.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookbrew.customer.service.client.UserClient;
import com.bookbrew.customer.service.dto.UserDTO;

@Service
public class UserService {

    @Autowired
    private UserClient userClient;

    public UserDTO findUserById(Long userId) {
        return userClient.getUserById(userId);
    }
}
