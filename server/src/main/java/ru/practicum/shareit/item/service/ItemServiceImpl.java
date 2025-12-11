package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dal.CommentRepository;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dal.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemResponseDto addItem(Long ownerId, ItemCreateDto itemCreateDto) {
        log.info("Create item: {} by user id: {}", itemCreateDto,  ownerId);

        User owner = existingUser(ownerId);

        ItemRequest request = null;
        if (itemCreateDto.getRequestId() != null) {
            request = itemRequestRepository.findById(itemCreateDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Item Request",  itemCreateDto.getRequestId()));
        }

        Item item = itemMapper.toItem(itemCreateDto, owner);
        item.setRequest(request);
        Item savedItem = itemRepository.save(item);

        log.info("Item with id: {} saved", savedItem.getId());

        return itemMapper.toItemResponseDto(savedItem);
    }

    @Override
    @Transactional
    public ItemResponseDto updateItem(Long ownerId, Long itemId, ItemUpdateDto itemUpdateDto) throws AccessDeniedException {
        log.info("Update itemUpdateDto id: {} by user id: {}", itemId, ownerId);

        Item existingItem = existingItem(itemId);

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Access denied!");
        }

        if (itemUpdateDto.getName() != null && !itemUpdateDto.getName().isEmpty()) {
            existingItem.setName(itemUpdateDto.getName());
        }
        if (itemUpdateDto.getDescription() != null && !itemUpdateDto.getDescription().isEmpty()) {
            existingItem.setDescription(itemUpdateDto.getDescription());
        }
        if (itemUpdateDto.getAvailable() != null) {
            existingItem.setAvailable(itemUpdateDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        log.info("Item with id: {} has been updated successfully", updatedItem.getId());

        return itemMapper.toItemResponseDto(updatedItem);
    }

    @Override
    @Transactional
    public void deleteItem(Long ownerId, Long itemId) throws AccessDeniedException {
        log.info("Delete item with id: {} by user id: {}", itemId, ownerId);

        Item item = existingItem(itemId);

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Access denied!");
        }

        itemRepository.deleteById(itemId);
        log.info("Item with id: {} successfully deleted", itemId);
    }

    @Override
    public ItemWithBookingsAndComments getItemById(Long userId, Long itemId) {
        log.info("Getting item {} for user {}", itemId, userId);

        Item item = existingItem(itemId);
        ItemWithBookingsAndComments itemDto = itemMapper.toItemWithBookingsAndComments(item);

        if (item.getOwner().getId().equals(userId)) {
            List<Booking> lastBookings = bookingRepository.findByItemIdAndEndBeforeOrderByEndDesc(itemId, LocalDateTime.now());
            if (!lastBookings.isEmpty()) {
                itemDto.setLastBooking(bookingMapper.toBookingForItemDto(lastBookings.getFirst()));
            }

            List<Booking> nextBookings = bookingRepository.findByItemIdAndStartAfterOrderByStartAsc(
                    itemId, LocalDateTime.now());
            if (!nextBookings.isEmpty()) {
                itemDto.setNextBooking(bookingMapper.toBookingForItemDto(nextBookings.getFirst()));
            }
        }

        List<Comment> comments = commentRepository.findByItemId(itemId);
        itemDto.setComments(comments.stream()
                .map(commentMapper::toCommentResponseDto)
                .collect(Collectors.toList()));

        log.info("Item with id: {} successfully retrieved", itemId);
        return itemDto;
    }

    @Override
    public List<ItemResponseDto> searchItems(String text) {
        log.info("Search items for text: {}", text);

        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<Item> items = itemRepository.searchAvailableItems(text.trim());
        List<ItemResponseDto> result = items.stream()
                .map(itemMapper::toItemResponseDto)
                .collect(Collectors.toList());

        log.info("Found {} items by search text: '{}'", items.size(), text);
        return result;
    }

    @Override
    public List<ItemWithBookingsAndComments> getItemForOwner(Long ownerId) {
        log.info("Getting items for owner id: {}", ownerId);

        existingUser(ownerId);

        List<Item> items = itemRepository.findByOwnerIdOrderById(ownerId);
        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, List<Booking>> lastBookingsMap = bookingRepository.findLastBookingsForItems(itemIds);
        Map<Long, List<Booking>> nextBookingsMap = bookingRepository.findNextBookingsForItems(itemIds);

        Map<Long, List<Comment>> commentsByItem = commentRepository.findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        List<ItemWithBookingsAndComments> result = items.stream()
                .map(item -> {
                    ItemWithBookingsAndComments dto = itemMapper.toItemWithBookingsAndComments(item);

                    List<Booking> lastBookings = lastBookingsMap.getOrDefault(item.getId(), Collections.emptyList());
                    List<Booking> nextBookings = nextBookingsMap.getOrDefault(item.getId(), Collections.emptyList());

                    if (!lastBookings.isEmpty()) {
                        dto.setLastBooking(bookingMapper.toBookingForItemDto(lastBookings.getFirst()));
                    }
                    if (!nextBookings.isEmpty()) {
                        dto.setNextBooking(bookingMapper.toBookingForItemDto(nextBookings.getFirst()));
                    }

                    List<Comment> itemComments = commentsByItem.getOrDefault(item.getId(), Collections.emptyList());
                    dto.setComments(itemComments.stream()
                            .map(commentMapper::toCommentResponseDto)
                            .collect(Collectors.toList()));

                    return dto;
                })
                .toList();
        log.info("Found {} items for owner id: {}", items.size(), ownerId);
        return result;
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long authorId, Long itemId, CommentCreateDto commentCreateDto) {
        log.info("Adding new comment for item with id: {} and comment: {}", itemId, commentCreateDto);

        User author = existingUser(authorId);
        Item item = existingItem(itemId);

        List<Booking> userBookings = bookingRepository
                .findByBookerIdAndItemIdAndEndBeforeAndStatus(authorId, itemId, LocalDateTime.now(), Status.APPROVED);
        if (userBookings.isEmpty()) {
            throw new ValidationException("User " + authorId + " can only comment on items they have booked in the past");
        }

        Comment comment = commentMapper.toComment(commentCreateDto, item, author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment with id: {} has been saved successfully", savedComment.getId());

        return commentMapper.toCommentResponseDto(savedComment);
    }

    private Item existingItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item", itemId));
    }

    private User existingUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
    }
}
