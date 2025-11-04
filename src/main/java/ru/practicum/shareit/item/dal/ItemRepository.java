package ru.practicum.shareit.item.dal;

import ru.practicum.shareit.item.model.Item;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface ItemRepository {
    Item addItem(Long userId, Item item);
    Item updateItem(Long userId, Long id, Item newItem) throws AccessDeniedException;
    void deleteItem(Long userId, Long id);
    Item getItem(Long id);
    List<Item> getItems(Long userId);
    List<Item> searchItems(String text);
}
