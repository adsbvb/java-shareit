package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface ItemService {
    Item addItem(Long userId, Item item);

    Item updateItem(Long userId, Long itemId, Item item) throws AccessDeniedException;

    void deleteItem(Long userId, Long itemId);

    Item getItemById(Long itemId);

    List<Item> getAllItems(Long userId);

    List<Item> searchItems(String text);
}
