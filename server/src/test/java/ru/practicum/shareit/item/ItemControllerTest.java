package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@DisplayName("ItemController tests (service module)")
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemCreateDto itemCreateDto;
    private ItemUpdateDto itemUpdateDto;
    private CommentCreateDto commentCreateDto;
    private ItemResponseDto itemResponseDto;
    private ItemWithBookingsAndComments itemWithDetails;
    private CommentResponseDto commentResponseDto;

    @BeforeEach
    void setUp() {
        itemCreateDto = ItemCreateDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .requestId(1L)
                .build();

        itemUpdateDto = ItemUpdateDto.builder()
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .build();

        commentCreateDto = CommentCreateDto.builder()
                .text("Test comment")
                .build();

        itemResponseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .requestId(1L)
                .build();

        itemWithDetails = ItemWithBookingsAndComments.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        commentResponseDto = CommentResponseDto.builder()
                .id(1L)
                .text("Test comment")
                .authorName("Test User")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void add_ValidData_ReturnsCreatedItem() throws Exception {
        when(itemService.addItem(anyLong(), any(ItemCreateDto.class)))
                .thenReturn(itemResponseDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void update_ValidData_ReturnsUpdatedItem() throws Exception {
        ItemResponseDto updatedItem = ItemResponseDto.builder()
                .id(1L)
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .build();

        when(itemService.updateItem(anyLong(), anyLong(), any(ItemUpdateDto.class)))
                .thenReturn(updatedItem);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Item"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void update_AccessDenied_ReturnsForbidden() throws Exception {
        when(itemService.updateItem(anyLong(), anyLong(), any(ItemUpdateDto.class)))
                .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_ValidData_ReturnsNoContent() throws Exception {
        doNothing().when(itemService).deleteItem(anyLong(), anyLong());

        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void delete_AccessDenied_ReturnsForbidden() throws Exception {
        doThrow(new AccessDeniedException("Access denied"))
                .when(itemService).deleteItem(anyLong(), anyLong());

        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_ValidData_ReturnsItem() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(itemWithDetails);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void search_ValidText_ReturnsItems() throws Exception {
        List<ItemResponseDto> items = List.of(itemResponseDto);

        when(itemService.searchItems(anyString()))
                .thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void getItemsForOwner_ValidUserId_ReturnsItems() throws Exception {
        List<ItemWithBookingsAndComments> items = List.of(itemWithDetails);

        when(itemService.getItemForOwner(anyLong()))
                .thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void addComment_ValidData_ReturnsComment() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any(CommentCreateDto.class)))
                .thenReturn(commentResponseDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Test comment"))
                .andExpect(jsonPath("$.authorName").value("Test User"));
    }
}