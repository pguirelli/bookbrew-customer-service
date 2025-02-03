package com.bookbrew.customer.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.bookbrew.customer.service.dto.UserDTO;
import com.bookbrew.customer.service.dto.UserProfileDTO;
import com.bookbrew.customer.service.dto.UserResponseDTO;

@FeignClient(name = "auth-service", url = "${auth.service.url}")
public interface UserClient {

    @PostMapping("/api/users")
    UserResponseDTO createUser(@RequestBody UserDTO userDTO);

    @GetMapping("/api/users/{userId}")
    UserResponseDTO getUserById(@PathVariable Long userId);

    @PutMapping("/api/users/{userId}")
    UserDTO updateUser(@PathVariable Long userId, @RequestBody UserDTO userDTO);

    @DeleteMapping("/api/users/{userId}")
    void deleteUser(@PathVariable Long userId);

    @GetMapping("/api/user-profiles/{id}")
    UserProfileDTO getUserProfileById(@PathVariable Long id);

}
