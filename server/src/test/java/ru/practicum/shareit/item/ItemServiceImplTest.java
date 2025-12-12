package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
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
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.dal.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemServiceImpl unit tests")
public class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository  itemRequestRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemRequest request;
    private ItemCreateDto itemCreateDto;
    private ItemUpdateDto itemUpdateDto;

    @BeforeEach
    public void setUp() {
        owner = User.builder()
                .id(1L)
                .name("Owner Name")
                .email("owner@example.com")
                .build();

        booker = User.builder()
                .id(2L)
                .name("Booker Name")
                .email("booker@example.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .owner(owner)
                .build();

        request = ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель")
                .requester(booker)
                .created(LocalDateTime.now().minusDays(1))
                .build();

        itemCreateDto = ItemCreateDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .requestId(1L)
                .build();

        itemUpdateDto = ItemUpdateDto.builder()
                .name("Обновленная дрель")
                .description("Новое описание")
                .available(false)
                .build();
    }

    @Test
    void addItem_whenValidData_shouldReturnSavedItem() {
        ItemResponseDto expectedDto = ItemResponseDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemMapper.toItem(itemCreateDto, owner)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toItemResponseDto(item)).thenReturn(expectedDto);

        ItemResponseDto result = itemService.addItem(1L, itemCreateDto);

        assertThat(result).isEqualTo(expectedDto);
        verify(itemRepository).save(item);
        verify(itemRequestRepository).findById(1L);
    }

    @Test
    void addItem_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addItem(999L, itemCreateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void updateItem_whenOwnerUpdates_shouldReturnUpdatedItem() throws AccessDeniedException {
        Item updatedItem = Item.builder()
                .id(1L)
                .name("Обновленная дрель")
                .description("Новое описание")
                .available(false)
                .owner(owner)
                .build();

        ItemResponseDto expectedDto = ItemResponseDto.builder()
                .id(1L)
                .name("Обновленная дрель")
                .description("Новое описание")
                .available(false)
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.toItemResponseDto(any(Item.class))).thenReturn(expectedDto);

        ItemResponseDto result = itemService.updateItem(1L, 1L, itemUpdateDto);

        assertThat(result.getName()).isEqualTo("Обновленная дрель");
        assertThat(result.getDescription()).isEqualTo("Новое описание");
        assertThat(result.getAvailable()).isFalse();
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_whenNotOwnerUpdates_shouldThrowAccessDeniedException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.updateItem(999L, 1L, itemUpdateDto))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access denied!");
    }

    @Test
    void updateItem_whenPartialUpdate_shouldUpdateOnlyProvidedFields() throws AccessDeniedException {
        ItemUpdateDto partialUpdateDto = ItemUpdateDto.builder()
                .name("Новое название")
                .build();

        Item updatedItem = Item.builder()
                .id(1L)
                .name("Новое название")
                .description("Аккумуляторная дрель")
                .available(true)
                .owner(owner)
                .build();

        ItemResponseDto expectedDto = ItemResponseDto.builder()
                .id(1L)
                .name("Новое название")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.toItemResponseDto(any(Item.class))).thenReturn(expectedDto);

        ItemResponseDto result = itemService.updateItem(1L, 1L, partialUpdateDto);

        assertThat(result.getName()).isEqualTo("Новое название");
        assertThat(result.getDescription()).isEqualTo("Аккумуляторная дрель");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void getItemById_whenOwnerRequests_shouldReturnWithBookings() {
            Long userId = 1L;
            Long itemId = 1L;

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime yesterday = now.minusDays(1);
            LocalDateTime tomorrow = now.plusDays(1);
            LocalDateTime twoDaysAgo = now.minusDays(2);
            LocalDateTime twoDaysLater = now.plusDays(2);

            Booking lastBooking = Booking.builder()
                    .id(1L)
                    .start(twoDaysAgo)
                    .end(yesterday)
                    .item(item)
                    .booker(booker)
                    .status(Status.APPROVED)
                    .build();

            Booking nextBooking = Booking.builder()
                    .id(2L)
                    .start(tomorrow)
                    .end(twoDaysLater)
                    .item(item)
                    .booker(booker)
                    .status(Status.APPROVED)
                    .build();

            Comment comment = Comment.builder()
                    .id(1L)
                    .text("Отличная дрель!")
                    .item(item)
                    .author(booker)
                    .created(yesterday)
                    .build();

            CommentResponseDto commentDto = CommentResponseDto.builder()
                    .id(1L)
                    .text("Отличная дрель!")
                    .authorName("Booker Name")
                    .created(yesterday)
                    .build();

            BookingForItemDto lastBookingDto = BookingForItemDto.builder()
                    .id(1L)
                    .bookerId(booker.getId())
                    .build();

            BookingForItemDto nextBookingDto = BookingForItemDto.builder()
                    .id(2L)
                    .bookerId(booker.getId())
                    .build();

            ItemWithBookingsAndComments expectedDto = ItemWithBookingsAndComments.builder()
                    .id(1L)
                    .name("Дрель")
                    .description("Аккумуляторная дрель")
                    .available(true)
                    .build();

            when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(itemMapper.toItemWithBookingsAndComments(item)).thenReturn(expectedDto);
            when(bookingRepository.findByItemIdAndEndBeforeOrderByEndDesc(
                    eq(itemId),
                    any(LocalDateTime.class)))
                    .thenReturn(List.of(lastBooking));
            when(bookingRepository.findByItemIdAndStartAfterOrderByStartAsc(
                    eq(itemId),
                    any(LocalDateTime.class)))
                    .thenReturn(List.of(nextBooking));
            when(commentRepository.findByItemId(itemId)).thenReturn(List.of(comment));
            when(commentMapper.toCommentResponseDto(comment)).thenReturn(commentDto);
            when(bookingMapper.toBookingForItemDto(lastBooking)).thenReturn(lastBookingDto);
            when(bookingMapper.toBookingForItemDto(nextBooking)).thenReturn(nextBookingDto);

            ItemWithBookingsAndComments result = itemService.getItemById(userId, itemId);

            assertThat(result).isNotNull();
            assertThat(result.getLastBooking()).isNotNull();
            assertThat(result.getNextBooking()).isNotNull();
            assertThat(result.getComments()).hasSize(1);
            assertThat(result.getComments().get(0).getText()).isEqualTo("Отличная дрель!");
    }

    @Test
    void getItemById_whenNonOwnerRequests_shouldReturnWithoutBookings() {
        Long userId = 999L;
        Long itemId = 1L;

        Comment comment = Comment.builder()
                .id(1L)
                .text("Отличная дрель!")
                .item(item)
                .author(booker)
                .created(LocalDateTime.now().minusHours(5))
                .build();

        CommentResponseDto commentDto = CommentResponseDto.builder()
                .id(1L)
                .text("Отличная дрель!")
                .authorName("Booker Name")
                .created(LocalDateTime.now().minusHours(5))
                .build();

        ItemWithBookingsAndComments expectedDto = ItemWithBookingsAndComments.builder()
                .id(1L)
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toItemWithBookingsAndComments(item)).thenReturn(expectedDto);
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of(comment));
        when(commentMapper.toCommentResponseDto(comment)).thenReturn(commentDto);

        ItemWithBookingsAndComments result = itemService.getItemById(userId, itemId);

        assertThat(result).isNotNull();
        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
        assertThat(result.getComments()).hasSize(1);
        verify(bookingRepository, never()).findByItemIdAndEndBeforeOrderByEndDesc(anyLong(), any());
        verify(bookingRepository, never()).findByItemIdAndStartAfterOrderByStartAsc(anyLong(), any());
    }

    @Test
    void searchItems_whenValidText_shouldReturnAvailableItems() {
        String searchText = "дрель";
        List<Item> items = List.of(item);
        ItemResponseDto itemDto = ItemResponseDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        when(itemRepository.searchAvailableItems(searchText)).thenReturn(items);
        when(itemMapper.toItemResponseDto(item)).thenReturn(itemDto);

        List<ItemResponseDto> result = itemService.searchItems(searchText);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase("дрель");
    }

    @Test
    void searchItems_whenEmptyText_shouldReturnEmptyList() {
        String searchText = "   ";

        List<ItemResponseDto> result = itemService.searchItems(searchText);

        assertThat(result).isEmpty();
        verify(itemRepository, never()).searchAvailableItems(anyString());
    }

    @Test
    void getItemForOwner_shouldReturnItemsWithBookingsAndComments() {
        Long ownerId = 1L;
        List<Item> items = List.of(item);

        Booking lastBooking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(booker)
                .status(Status.APPROVED)
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .text("Хорошая вещь")
                .item(item)
                .author(booker)
                .created(LocalDateTime.now().minusDays(1))
                .build();

        CommentResponseDto commentDto = CommentResponseDto.builder()
                .id(1L)
                .text("Хорошая вещь")
                .authorName("Booker Name")
                .created(LocalDateTime.now().minusDays(1))
                .build();

        BookingForItemDto lastBookingDto = BookingForItemDto.builder()
                .id(1L)
                .bookerId(booker.getId())
                .build();

        ItemWithBookingsAndComments expectedDto = ItemWithBookingsAndComments.builder()
                .id(1L)
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerIdOrderById(ownerId)).thenReturn(items);
        when(bookingRepository.findLastBookingsForItems(List.of(1L)))
                .thenReturn(Map.of(1L, List.of(lastBooking)));
        when(bookingRepository.findNextBookingsForItems(List.of(1L)))
                .thenReturn(Map.of(1L, List.of()));
        when(commentRepository.findByItemIdIn(List.of(1L)))
                .thenReturn(List.of(comment));
        when(itemMapper.toItemWithBookingsAndComments(item)).thenReturn(expectedDto);
        when(commentMapper.toCommentResponseDto(comment)).thenReturn(commentDto);
        when(bookingMapper.toBookingForItemDto(lastBooking)).thenReturn(lastBookingDto);

        List<ItemWithBookingsAndComments> result = itemService.getItemForOwner(ownerId);

        assertThat(result).hasSize(1);

        verify(bookingMapper).toBookingForItemDto(lastBooking);

        ItemWithBookingsAndComments actualDto = result.get(0);
        assertThat(actualDto.getLastBooking())
                .isNotNull()
                .extracting(BookingForItemDto::getId)
                .isEqualTo(1L);
        assertThat(actualDto.getNextBooking()).isNull();
        assertThat(actualDto.getComments()).hasSize(1);
        assertThat(actualDto.getComments().get(0).getText()).isEqualTo("Хорошая вещь");
    }

    @Test
    void addComment_whenValidBookingExists_shouldReturnSavedComment() {
        Long authorId = 2L;
        Long itemId = 1L;

        CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .text("Отличная дрель!")
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(booker)
                .status(Status.APPROVED)
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .text("Отличная дрель!")
                .item(item)
                .author(booker)
                .created(LocalDateTime.now())
                .build();

        CommentResponseDto expectedDto = CommentResponseDto.builder()
                .id(1L)
                .text("Отличная дрель!")
                .authorName("Booker Name")
                .created(LocalDateTime.now())
                .build();

        when(userRepository.findById(authorId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndItemIdAndEndBeforeAndStatus(
                eq(authorId), eq(itemId), any(LocalDateTime.class), eq(Status.APPROVED)))
                .thenReturn(List.of(booking));
        when(commentMapper.toComment(commentCreateDto, item, booker)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toCommentResponseDto(comment)).thenReturn(expectedDto);

        CommentResponseDto result = itemService.addComment(authorId, itemId, commentCreateDto);

        assertThat(result.getText()).isEqualTo("Отличная дрель!");
        assertThat(result.getAuthorName()).isEqualTo("Booker Name");
        verify(commentRepository).save(comment);
    }

    @Test
    void deleteItem_whenOwnerDeletes_shouldDeleteSuccessfully() throws AccessDeniedException {
        Long ownerId = 1L;
        Long itemId = 1L;

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        doNothing().when(itemRepository).deleteById(itemId);

        itemService.deleteItem(ownerId, itemId);

        verify(itemRepository).deleteById(itemId);
    }

    @Test
    void deleteItem_whenNonOwnerDeletes_shouldThrowAccessDeniedException() {
        Long nonOwnerId = 999L;
        Long itemId = 1L;

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.deleteItem(nonOwnerId, itemId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access denied!");
    }
}

