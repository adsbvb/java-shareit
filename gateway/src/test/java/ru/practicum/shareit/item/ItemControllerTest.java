package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@DisplayName("ItemController tests (gateway module)")
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    private ItemCreateDto itemCreateDto;
    private ItemUpdateDto itemUpdateDto;
    private CommentCreateDto commentCreateDto;

    @BeforeEach
    void setUp() {
        itemCreateDto = ItemCreateDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        itemUpdateDto = ItemUpdateDto.builder()
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .build();

        commentCreateDto = CommentCreateDto.builder()
                .text("Test comment")
                .build();
    }

    @Test
    void addItem_ValidData_ReturnsOk() throws Exception {
        when(itemClient.addItem(anyLong(), any(ItemCreateDto.class)))
                .thenReturn(new ResponseEntity<>("Item created", HttpStatus.OK));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Item created"));
    }

    @Test
    void addItem_InvalidUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_MissingUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_InvalidItemData_ReturnsBadRequest() throws Exception {
        ItemCreateDto invalidItem = ItemCreateDto.builder()
                .name("")
                .description("")
                .available(null)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_ValidData_ReturnsOk() throws Exception {
        when(itemClient.updateItem(anyLong(), anyLong(), any(ItemUpdateDto.class)))
                .thenReturn(new ResponseEntity<>("Item updated", HttpStatus.OK));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Item updated"));
    }

    @Test
    void updateItem_InvalidItemId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/items/0")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/items/-1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_InvalidUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteItem_ValidData_ReturnsOk() throws Exception {
        when(itemClient.deleteItem(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void deleteItem_InvalidIds_ReturnsBadRequest() throws Exception {
        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 0L))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/items/0")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemById_ValidData_ReturnsOk() throws Exception {
        when(itemClient.getItemById(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>("Item details", HttpStatus.OK));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("Item details"));
    }

    @Test
    void getItemById_InvalidIds_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 0L))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items/0")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchItems_ValidText_ReturnsOk() throws Exception {
        when(itemClient.searchItems(anyString()))
                .thenReturn(new ResponseEntity<>("Search results", HttpStatus.OK));

        mockMvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Search results"));

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk());
    }

    @Test
    void getItemsForOwner_ValidUserId_ReturnsOk() throws Exception {
        when(itemClient.getItemsForOwner(anyLong()))
                .thenReturn(new ResponseEntity<>("Owner items", HttpStatus.OK));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("Owner items"));
    }

    @Test
    void getItemsForOwner_InvalidUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 0L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_ValidData_ReturnsOk() throws Exception {
        when(itemClient.addComment(anyLong(), anyLong(), any(CommentCreateDto.class)))
                .thenReturn(new ResponseEntity<>("Comment added", HttpStatus.OK));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Comment added"));
    }

    @Test
    void addComment_InvalidData_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/items/0/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isBadRequest());

        CommentCreateDto invalidComment = CommentCreateDto.builder()
                .text("")
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidComment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_MissingUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isBadRequest());
    }
}