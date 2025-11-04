package ru.practicum.shareit.user.dal;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    User createUser(User user);
    User updateUser(Long userId, User user);
    void deleteUser(Long userId);
    User findUser(Long userId);
    List<User> getUsers();
}
