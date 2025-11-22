package ru.practicum.shareit.item;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemResponseDto add(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @RequestBody @Validated ItemCreateDto itemDto
    ) {
        log.debug("Adding new item");
        return itemService.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @PathVariable(name = "itemId") @Positive(message = "Item id must be a positive number") Long itemId,
            @RequestBody @Validated ItemUpdateDto itemDto
    ) throws AccessDeniedException {
        log.debug("Updating item");
        return  itemService.updateItem(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public void delete(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @PathVariable(name = "itemId") @Positive(message = "Item id must be a positive number") Long itemId
    ) throws AccessDeniedException {
        log.debug("Deleting item");
        itemService.deleteItem(userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsAndComments getById(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @PathVariable(name = "itemId") @Positive(message = "Item id must be a positive number") Long itemId
    ) {
        log.debug("Retrieving item");
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> search(
            @RequestParam String text
    ) {
        log.debug("Searching items");
        return itemService.searchItems(text);
    }

    @GetMapping
    public List<ItemWithBookingsAndComments> getItemsForOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId
    ) {
        log.debug("Searching items for owner");
        return itemService.getItemForOwner(userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @PathVariable(name = "itemId") @Positive(message = "Item id must be a positive number") Long itemId,
            @RequestBody CommentCreateDto comment
    ) {
        log.debug("Adding new comment");
        return itemService.addComment(userId, itemId, comment);
    }
}
