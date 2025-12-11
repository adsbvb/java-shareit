package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;


@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ru.practicum.shareit.item.ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @RequestBody @Valid ItemCreateDto itemDto
    ) {
        log.info("Gateway: POST /items - add item by user {}", userId);
        return itemClient.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @PathVariable @Positive(message = "Item id must be a positive number") Long itemId,
            @RequestBody @Valid ItemUpdateDto itemDto
    ) {
        log.info("Gateway: PATCH /items/{} - update item by user {}", itemId, userId);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @PathVariable @Positive(message = "Item id must be a positive number") Long itemId
    ) {
        log.info("Gateway: DELETE /items/{} - delete item by user {}", itemId, userId);
        return itemClient.deleteItem(userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @PathVariable @Positive(message = "Item id must be a positive number") Long itemId
    ) {
        log.info("Gateway: GET /items/{} - get item by user {}", itemId, userId);
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(
            @RequestParam String text
    ) {
        log.info("Gateway: GET /items/search - search items with text: {}", text);
        return itemClient.searchItems(text);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsForOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId
    ) {
        log.info("Gateway: GET /items - get items for owner {}", userId);
        return itemClient.getItemsForOwner(userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @PathVariable @Positive(message = "Item id must be a positive number") Long itemId,
            @RequestBody @Valid CommentCreateDto commentDto
    ) {
        log.info("Gateway: POST /items/{}/comment - add comment by user {}", itemId, userId);
        return itemClient.addComment(userId, itemId, commentDto);
    }
}