package com.example.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Cacheable(value = "myCache", key = "'myCache'")
    @GetMapping
    public List<MyUser> getAllUsers() {
        return userRepository.findAll();
    }

    @CacheEvict(value = "usersCache", key = "'allUsers'", beforeInvocation = true)
    @PostMapping
    public MyUser addUser(@RequestBody MyUser myUser) {
        return userRepository.save(myUser);
    }
}