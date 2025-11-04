package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.error.validation.CreateGroup;
import ru.practicum.shareit.error.validation.PatchUpdateGroup;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.file.AccessDeniedException;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto add(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody @Validated(CreateGroup.class) ItemDto itemDto
    ) {
        Item item = ItemMapper.mapToItem(itemDto);
        Item addedItem = itemService.addItem(userId, item);
        log.info("Added new item name {} by user id {}", itemDto.getName(), userId);
        return ItemMapper.mapToItemDto(addedItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable(name="itemId") Long itemId,
            @RequestBody @Validated(PatchUpdateGroup.class) ItemDto itemDto
    ) throws AccessDeniedException {
        Item item = ItemMapper.mapToItem(itemDto);
        Item updatedItem = itemService.updateItem(userId, itemId, item);
        log.info("Updated item id {} by user id {}", updatedItem.getId(), userId);
        return ItemMapper.mapToItemDto(updatedItem);
    }

    @DeleteMapping("/{itemId}")
    public void delete(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable(name="itemId") Long itemId
    ) {
        itemService.deleteItem(userId, itemId);
        log.info("Deleted item id {} by user id {}", itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(
            @PathVariable(name="itemId") Long itemId
    ) {
        Item item = itemService.getItemById(itemId);
        log.info("Received item: {}", item);
        return ItemMapper.mapToItemDto(item);
    }

    @GetMapping
    public List<ItemDto> getAll(
            @RequestHeader("X-Sharer-User-Id") Long userId
    ) {
        List<Item> items = itemService.getAllItems(userId);
        log.info("Found {} items", items.size());
        return items.stream()
                .map(ItemMapper::mapToItemDto)
                .toList();
    }

    @GetMapping("/search")
    public List<ItemDto> search(
            @RequestParam String text
    ) {
        List<Item> items = itemService.searchItems(text);
        log.info("Found {} items by search text: '{}'", items.size(), text);
        return items.stream()
                .map(ItemMapper::mapToItemDto)
                .toList();
    }
}
