package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RequestController.class)
@DisplayName("RequestController tests (gateway module)")
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestClient requestClient;

    private ItemRequestDto itemRequestDto;
    private String itemRequestJson;

    @BeforeEach
    void setUp() throws Exception {
        itemRequestDto = ItemRequestDto.builder()
                .description("Нужна дрель для ремонта дома")
                .build();

        itemRequestJson = objectMapper.writeValueAsString(itemRequestDto);
    }

    @Test
    void createRequest_ValidData_ReturnsCreated() throws Exception {
        String responseBody = """
                {
                    "id": 1,
                    "description": "Нужна дрель для ремонта",
                    "created": "2025-01-15T10:30:00",
                    "items": []
                }
                """;
        ResponseEntity<Object> responseEntity = ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseBody);

        when(requestClient.createRequest(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(responseEntity);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemRequestJson))
                .andExpect(status().isCreated())
                .andExpect(content().json(responseBody));
    }

    @Test
    void createRequest_InvalidUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemRequestJson))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemRequestJson))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequest_InvalidRequestData_ReturnsBadRequest() throws Exception {
        ItemRequestDto invalidDto = ItemRequestDto.builder()
                .description("")
                .build();

        String invalidJson = objectMapper.writeValueAsString(invalidDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        invalidDto = ItemRequestDto.builder()
                .description(null)
                .build();

        invalidJson = objectMapper.writeValueAsString(invalidDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserRequests_ValidUserId_ReturnsOk() throws Exception {
        String responseBody = """
                [
                    {
                        "id": 1,
                        "description": "Нужна дрель",
                        "created": "2025-01-15T10:30:00",
                        "items": []
                    },
                    {
                        "id": 2,
                        "description": "Нужен молоток",
                        "created": "2025-01-16T11:30:00",
                        "items": []
                    }
                ]
                """;
        ResponseEntity<Object> responseEntity = ResponseEntity.ok(responseBody);

        when(requestClient.getUserRequests(anyLong()))
                .thenReturn(responseEntity);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));
    }

    @Test
    void getUserRequests_InvalidUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 0L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequests_ValidParams_ReturnsOk() throws Exception {
        String responseBody = """
                [
                    {
                        "id": 1,
                        "description": "Нужна дрель",
                        "created": "2025-01-15T10:30:00",
                        "items": []
                    }
                ]
                """;
        ResponseEntity<Object> responseEntity = ResponseEntity.ok(responseBody);

        when(requestClient.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(responseEntity);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));
    }

    @Test
    void getAllRequests_InvalidParams_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 0L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "-5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestById_ValidIds_ReturnsOk() throws Exception {
        String responseBody = """
                {
                    "id": 1,
                    "description": "Нужна дрель",
                    "created": "2025-01-15T10:30:00",
                    "items": [
                        {
                            "id": 1,
                            "name": "Электрическая дрель",
                            "description": "Powerful electric drill",
                            "available": true,
                            "requestId": 1
                        }
                    ]
                }
                """;
        ResponseEntity<Object> responseEntity = ResponseEntity.ok(responseBody);

        when(requestClient.getRequestById(anyLong(), anyLong()))
                .thenReturn(responseEntity);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));
    }

    @Test
    void getRequestById_InvalidIds_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 0L))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/requests/0")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/requests/-1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isBadRequest());
    }
}