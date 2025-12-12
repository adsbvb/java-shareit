package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController tests (service module)")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserCreateDto userCreateDto;
    private UserUpdateDto userUpdateDto;
    private UserDto userDto;
    private String userJson;
    private String userUpdateJson;

    @BeforeEach
    void setUp() throws Exception {
        userCreateDto = UserCreateDto.builder()
                .name("Test User")
                .email("test@example.com")
                .build();

        userUpdateDto = UserUpdateDto.builder()
                .name("Updated User")
                .email("updated@example.com")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        userJson = objectMapper.writeValueAsString(userCreateDto);
        userUpdateJson = objectMapper.writeValueAsString(userUpdateDto);
    }

    @Test
    void create_ValidData_ShouldReturnUserDto() throws Exception {
        when(userService.createUser(any(UserCreateDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).createUser(any(UserCreateDto.class));
    }

    @Test
    void update_ValidData_ShouldReturnUpdatedUserDto() throws Exception {
        Long userId = 1L;
        UserDto updatedUserDto = UserDto.builder()
                .id(1L)
                .name("Updated User")
                .email("updated@example.com")
                .build();

        when(userService.updateUser(eq(userId), any(UserUpdateDto.class)))
                .thenReturn(updatedUserDto);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService, times(1)).updateUser(eq(userId), any(UserUpdateDto.class));
    }

    @Test
    void delete_ValidUserId_ShouldReturnNoContent() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void getById_ValidUserId_ShouldReturnUserDto() throws Exception {
        Long userId = 1L;
        when(userService.getUserById(userId)).thenReturn(userDto);

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getAll_ShouldReturnListOfUsers() throws Exception {
        List<UserDto> users = List.of(
                userDto,
                UserDto.builder()
                        .id(2L)
                        .name("User 2")
                        .email("user2@example.com")
                        .build()
        );

        when(userService.getAllUser()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test User"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("User 2"));

        verify(userService, times(1)).getAllUser();
    }

    @Test
    void update_PartialUpdate_ShouldWork() throws Exception {
        Long userId = 1L;
        UserUpdateDto partialDto = UserUpdateDto.builder()
                .name("Updated Name")
                .email(null)
                .build();
        String partialJson = objectMapper.writeValueAsString(partialDto);

        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .name("Updated Name")
                .email("test@example.com")
                .build();

        when(userService.updateUser(eq(userId), any(UserUpdateDto.class)))
                .thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(partialJson))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUser(eq(userId), any(UserUpdateDto.class));
    }
}