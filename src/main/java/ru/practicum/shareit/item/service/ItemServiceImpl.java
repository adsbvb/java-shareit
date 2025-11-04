package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserRepository;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Item addItem(Long userId, Item item) {
        userRepository.findUser(userId);
        return itemRepository.addItem(userId, item);
    }

    @Override
    public Item updateItem(Long userId, Long itemId, Item item) throws AccessDeniedException {
        return itemRepository.updateItem(userId, itemId, item);
    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        itemRepository.deleteItem(userId, itemId);
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.getItem(itemId);
    }

    @Override
    public List<Item> getAllItems(Long userId) {
        return itemRepository.getItems(userId);
    }

    @Override
    public List<Item> searchItems(String text) {
        return itemRepository.searchItems(text);
    }
}
