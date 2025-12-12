package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserCreateDto userCreateDto);

    UserDto updateUser(Long userid, UserUpdateDto userUpdateDto);

    void deleteUser(Long userId);

    UserDto getUserById(Long id);

    List<UserDto> getAllUser();
}
