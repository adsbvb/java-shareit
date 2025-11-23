package ru.practicum.shareit.user;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.error.validation.CreateGroup;
import ru.practicum.shareit.error.validation.PatchUpdateGroup;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public UserDto create(
            @RequestBody @Validated(CreateGroup.class) UserDto userDto
    ) {
        User user = userMapper.toUser(userDto);
        User createdUser = userService.createUser(user);
        log.info("Created user with id {}", createdUser.getId());
        return userMapper.toUserDto(createdUser);
    }

    @PatchMapping("/{userId}")
    public UserDto update(
            @PathVariable(name = "userId") @Positive(message = "User id must be a positive number") Long userId,
            @RequestBody @Validated(PatchUpdateGroup.class) UserDto userDto
    ) {
        User user = userMapper.toUser(userDto);
        User updatedUser = userService.updateUser(userId, user);
        log.info("Updated user with id {}", updatedUser.getId());
        return userMapper.toUserDto(updatedUser);
    }

    @DeleteMapping("/{userId}")
    public void delete(
            @PathVariable(name = "userId") @Positive(message = "User id must be a positive number") Long userId
    ) {
        userService.deleteUser(userId);
        log.info("User with id {} was deleted", userId);
    }

    @GetMapping("/{userId}")
    public UserDto getById(
            @PathVariable(name = "userId") @Positive(message = "User id must be a positive number") Long userId
    ) {
        User user = userService.getUserById(userId);
        log.info("User with id {} was fetched", userId);
        return userMapper.toUserDto(user);
    }

    @GetMapping
    public List<UserDto> getAll() {
        List<User> users = userService.getAllUser();
        log.info("Found {} users", users.size());
        return users.stream()
                .map(userMapper::toUserDto)
                .toList();
    }
}
