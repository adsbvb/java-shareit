package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController tests (gateway module)")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    private UserCreateDto userCreateDto;
    private UserUpdateDto userUpdateDto;
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

        userJson = objectMapper.writeValueAsString(userCreateDto);
        userUpdateJson = objectMapper.writeValueAsString(userUpdateDto);
    }

    @Test
    void create_ValidData_ShouldReturnResponseEntity() throws Exception {
        String responseBody = """
                {
                    "id": 1,
                    "name": "Test User",
                    "email": "test@example.com"
                }
                """;
        ResponseEntity<Object> responseEntity = ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseBody);

        when(userClient.createUser(any(UserCreateDto.class))).thenReturn(responseEntity);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(content().json(responseBody));
    }

    @Test
    void create_InvalidData_ShouldReturnBadRequest() throws Exception {
        UserCreateDto invalidDto = UserCreateDto.builder()
                .name("")
                .email("invalid-email")
                .build();

        String invalidJson = objectMapper.writeValueAsString(invalidDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_NoRequestBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_NoEmail_ShouldReturnBadRequest() throws Exception {
        UserCreateDto dtoWithoutEmail = UserCreateDto.builder()
                .name("Test User")
                .email(null)
                .build();

        String jsonWithoutEmail = objectMapper.writeValueAsString(dtoWithoutEmail);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutEmail))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users - должен вернуть ошибку при отсутствии имени")
    void create_NoName_ShouldReturnBadRequest() throws Exception {
        UserCreateDto dtoWithoutName = UserCreateDto.builder()
                .name(null)
                .email("test@example.com")
                .build();

        String jsonWithoutName = objectMapper.writeValueAsString(dtoWithoutName);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutName))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_ValidData_ShouldReturnOk() throws Exception {
        Long userId = 1L;
        String responseBody = """
                {
                    "id": 1,
                    "name": "Updated User",
                    "email": "updated@example.com"
                }
                """;
        ResponseEntity<Object> responseEntity = ResponseEntity
                .ok(responseBody);

        when(userClient.updateUser(eq(userId), any(UserUpdateDto.class)))
                .thenReturn(responseEntity);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateJson))
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));
    }

    @Test
    void update_InvalidData_ShouldReturnBadRequest() throws Exception {
        Long userId = 1L;
        UserUpdateDto invalidDto = UserUpdateDto.builder()
                .name("")
                .email("invalid-email")
                .build();

        String invalidJson = objectMapper.writeValueAsString(invalidDto);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_ValidUserId_ShouldReturnOk() throws Exception {
        Long userId = 1L;
        ResponseEntity<Object> responseEntity = ResponseEntity
                .ok("User deleted successfully");

        when(userClient.deleteUser(userId)).thenReturn(responseEntity);

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    void getById_ValidUserId_ShouldReturnOk() throws Exception {
        Long userId = 1L;
        String userResponse = """
                {
                    "id": 1,
                    "name": "Test User",
                    "email": "test@example.com"
                }
                """;

        ResponseEntity<Object> responseEntity = ResponseEntity
                .ok(userResponse);

        when(userClient.getUserById(userId)).thenReturn(responseEntity);

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(userResponse));
    }

    @Test
    void getAll_ShouldReturnOk() throws Exception {
        String usersResponse = """
                [
                    {
                        "id": 1,
                        "name": "User 1",
                        "email": "user1@example.com"
                    },
                    {
                        "id": 2,
                        "name": "User 2",
                        "email": "user2@example.com"
                    }
                ]
                """;

        ResponseEntity<Object> responseEntity = ResponseEntity
                .ok(usersResponse);

        when(userClient.getAllUsers()).thenReturn(responseEntity);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(usersResponse));
    }
}