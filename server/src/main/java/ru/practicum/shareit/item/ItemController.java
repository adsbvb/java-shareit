package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemResponseDto add(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemCreateDto itemDto
    ) {
        log.info("Service: POST /items - add item by user {}", userId);
        return itemService.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable(name = "itemId") Long itemId,
            @RequestBody ItemUpdateDto itemDto
    ) throws AccessDeniedException {
        log.info("Service: PUT /items/{itemId} - update item {}", itemId);
        return  itemService.updateItem(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public void delete(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable(name = "itemId") Long itemId
    ) throws AccessDeniedException {
        log.info("Service: DELETE /items/{itemId} - remove item {}", itemId);
        itemService.deleteItem(userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsAndComments getById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable(name = "itemId") Long itemId
    ) {
        log.info("Service: GET /items/{itemId} - by user {}", itemId);
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> search(
            @RequestParam String text
    ) {
        log.info("Service: GET /search - search {}", text);
        return itemService.searchItems(text);
    }

    @GetMapping
    public List<ItemWithBookingsAndComments> getItemsForOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId
    ) {
        log.info("Service: GET /itemsForOwner/{userId} - for user {}", userId);
        return itemService.getItemForOwner(userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable(name = "itemId") Long itemId,
            @RequestBody CommentCreateDto comment
    ) {
        log.info("Service: POST /items/{itemId}/comment - add comment {}", itemId);
        return itemService.addComment(userId, itemId, comment);
    }
}
