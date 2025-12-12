package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserServiceImpl.updateUser() integration tests")
public class UserServiceImplIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    private User existingUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        existingUser = userRepository.save(User.builder()
                .name("User Name")
                .email("test@example.com")
                .build());
    }

    @Test
    void updateUser_whenUpdatingEmail_shouldUpdateSuccessfully() {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("updated@example.com")
                .build();

        UserDto result = userService.updateUser(existingUser.getId(), updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingUser.getId());
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getName()).isEqualTo("User Name");

        User updatedUser = userRepository.findById(existingUser.getId()).orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getName()).isEqualTo("User Name");
    }

    @Test
    void updateUser_whenUpdatingName_shouldUpdateSuccessfully() {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("Updated Name")
                .build();

        UserDto result = userService.updateUser(existingUser.getId(), updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingUser.getId());
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        User updatedUser = userRepository.findById(existingUser.getId()).orElseThrow();
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void updateUser_whenUpdatingEmailAndName_shouldUpdateBoth() {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("test.new@example.com")
                .name("Updated Name")
                .build();

        UserDto result = userService.updateUser(existingUser.getId(), updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test.new@example.com");
        assertThat(result.getName()).isEqualTo("Updated Name");

        User updatedUser = userRepository.findById(existingUser.getId()).orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo("test.new@example.com");
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    void updateUser_whenEmailAlreadyExists_shouldThrowException() {
        userRepository.save(User.builder()
                .name("Another User")
                .email("another@example.com")
                .build());

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("another@example.com")
                .build();

        assertThatThrownBy(() -> userService.updateUser(existingUser.getId(), updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

        User unchangedUser = userRepository.findById(existingUser.getId()).orElseThrow();
        assertThat(unchangedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(unchangedUser.getName()).isEqualTo("User Name");
    }

    @Test
    void updateUser_whenUserNotFound_shouldThrowNotFoundException() {
        Long nonExistentUserId = 999L;
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("new@example.com")
                .build();

        assertThatThrownBy(() -> userService.updateUser(nonExistentUserId, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User");
    }
}