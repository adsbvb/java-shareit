package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
@DisplayName("ItemRequestController tests (service module)")
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private ItemRequestDto itemRequestDto;
    private ItemRequestResponseDto itemRequestResponseDto;
    private ItemRequestResponseDto.ItemDto itemDto;
    private String itemRequestJson;

    @BeforeEach
    void setUp() throws Exception {
        itemRequestDto = ItemRequestDto.builder()
                .description("Нужна дрель для ремонта")
                .build();

        itemDto = ItemRequestResponseDto.ItemDto.builder()
                .id(1L)
                .name("Электрическая дрель")
                .ownerId(1L)
                .build();

        itemRequestResponseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Нужна дрель для ремонта")
                .created(LocalDateTime.now().minusDays(1))
                .items(List.of(itemDto))
                .build();

        itemRequestJson = objectMapper.writeValueAsString(itemRequestDto);
    }

    @Test
    void createRequest_ValidData_ReturnsCreatedRequest() throws Exception {
        when(itemRequestService.createRequest(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(itemRequestResponseDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Нужна дрель для ремонта"))
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[0].name").value("Электрическая дрель"))
                .andExpect(jsonPath("$.items[0].ownerId").value(1L));
    }

    @Test
    void getUserRequests_ValidUserId_ReturnsUserRequests() throws Exception {
        ItemRequestResponseDto request2 = ItemRequestResponseDto.builder()
                .id(2L)
                .description("Нужен молоток")
                .created(LocalDateTime.now().minusHours(5))
                .items(List.of())
                .build();

        List<ItemRequestResponseDto> requests = List.of(itemRequestResponseDto, request2);

        when(itemRequestService.getUserRequests(anyLong()))
                .thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель для ремонта"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].description").value("Нужен молоток"));
    }

    @Test
    void getAllRequests_WithPagination_ReturnsPaginatedRequests() throws Exception {
        ItemRequestResponseDto request2 = ItemRequestResponseDto.builder()
                .id(2L)
                .description("Нужен шпатель")
                .created(LocalDateTime.now().minusHours(2))
                .items(List.of())
                .build();

        List<ItemRequestResponseDto> requests = List.of(request2);

        when(itemRequestService.getAllRequests(anyLong(), eq(0), eq(10)))
                .thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].description").value("Нужен шпатель"));
    }

    @Test
    void getRequestById_ValidIds_ReturnsRequest() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenReturn(itemRequestResponseDto);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Нужна дрель для ремонта"))
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[0].name").value("Электрическая дрель"));
    }

    @Test
    void getRequestById_RequestNotFound_ReturnsNotFound() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Request", 999L));

        mockMvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRequest_UserNotFound_ReturnsNotFound() throws Exception {
        when(itemRequestService.createRequest(anyLong(), any(ItemRequestDto.class)))
                .thenThrow(new NotFoundException("User", 999L));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemRequestJson))
                .andExpect(status().isNotFound());
    }
}