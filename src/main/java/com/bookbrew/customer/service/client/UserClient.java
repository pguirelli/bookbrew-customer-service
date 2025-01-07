package com.bookbrew.customer.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.bookbrew.customer.service.dto.UserDTO;

@FeignClient(name = "auth-service", url = "${auth.service.url}")
public interface UserClient {

    @PostMapping("/api/users")
    UserDTO createUser(@RequestBody UserDTO userDTO);

    @GetMapping("/api/users/{userId}")
    UserDTO getUserById(@PathVariable Long userId);

    @PutMapping("/api/users/{userId}")
    UserDTO updateUser(@PathVariable("userId") Long userId, @RequestBody UserDTO userDTO);

}
