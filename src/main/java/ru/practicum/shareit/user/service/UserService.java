package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User createUser(User user);

    User updateUser(Long userid,User user);

    void deleteUser(Long userId);

    User getUserById(Long id);

    List<User> getAllUser();
}
