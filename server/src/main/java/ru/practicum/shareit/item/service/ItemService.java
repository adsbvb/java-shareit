package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface ItemService {
    ItemResponseDto addItem(Long userId, ItemCreateDto dto);

    ItemResponseDto updateItem(Long ownerId, Long itemId, ItemUpdateDto dto) throws AccessDeniedException;

    void deleteItem(Long userId, Long itemId) throws AccessDeniedException;

    ItemWithBookingsAndComments getItemById(Long userId, Long itemId);

    List<ItemResponseDto> searchItems(String text);

    List<ItemWithBookingsAndComments> getItemForOwner(Long ownerId);

    CommentResponseDto addComment(Long authorId, Long itemId, CommentCreateDto commentCreateDto);
}
