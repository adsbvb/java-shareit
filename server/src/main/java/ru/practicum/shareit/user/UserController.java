package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto create(
            @RequestBody UserCreateDto userDto
    ) {
        log.info("Service: POST /users - create user {}", userDto);
        return userService.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(
            @PathVariable(name = "userId") Long userId,
            @RequestBody UserUpdateDto userDto
    ) {
        log.info("Service: PUT /users/{userId} - update user {}", userDto);
        return userService.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public void delete(
            @PathVariable(name = "userId") Long userId
    ) {
        log.info("Service: DELETE /users/{userId} - delete user {}", userId);
        userService.deleteUser(userId);

    }

    @GetMapping("/{userId}")
    public UserDto getById(
            @PathVariable(name = "userId") Long userId
    ) {
        log.info("Service: GET /users/{userId} - get user {}", userId);
        return userService.getUserById(userId);
    }

    @GetMapping
    public List<UserDto> getAll() {
        log.info("Service: GET /users - get users");
        return userService.getAllUser();
    }
}
