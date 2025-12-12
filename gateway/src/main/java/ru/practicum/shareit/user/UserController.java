package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final ru.practicum.shareit.user.UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestBody @Valid UserCreateDto userDto
    ) {
        log.info("Gateway: POST /users - create user {}", userDto);
        return userClient.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(
            @PathVariable(name = "userId") @Positive(message = "User id must be a positive number") Long userId,
            @RequestBody @Valid UserUpdateDto userDto
    ) {
        log.info("Gateway: PUT /users - create user {}", userDto);
        return userClient.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(
            @PathVariable(name = "userId") @Positive(message = "User id must be a positive number") Long userId
    ) {
        log.info("Gateway: DELETE /users - delete user {}", userId);
        return userClient.deleteUser(userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getById(
            @PathVariable(name = "userId") @Positive(message = "User id must be a positive number") Long userId
    ) {
        log.info("Gateway: GET /users - get user {}", userId);
        return userClient.getUserById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Gateway: GET /users - get users");
        return userClient.getAllUsers();
    }
}
