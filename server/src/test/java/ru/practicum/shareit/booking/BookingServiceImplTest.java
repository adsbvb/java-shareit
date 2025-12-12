package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServiceImpl unit tests")
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingRequestDto bookingRequestDto;
    private BookingResponseDto bookingResponseDto;
    private LocalDateTime now;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        start = now.plusDays(1);
        end = now.plusDays(3);

        booker = User.builder()
                .id(1L)
                .name("Booker Name")
                .email("booker@example.com")
                .build();

        owner = User.builder()
                .id(2L)
                .name("Owner Name")
                .email("owner@example.com")
                .build();

        item = Item.builder()
                .id(10L)
                .name("Item name")
                .description("Test Description")
                .available(true)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(100L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(Status.WAITING)
                .build();

        bookingRequestDto = BookingRequestDto.builder()
                .itemId(10L)
                .start(start)
                .end(end)
                .build();

        bookingResponseDto = BookingResponseDto.builder()
                .id(100L)
                .start(start)
                .end(end)
                .status(Status.WAITING)
                .build();
    }

    @Test
    void addBooking_ValidData_ShouldCreateBooking() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.addBooking(1L, bookingRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(Status.WAITING);

        verify(userRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(10L);
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingMapper, times(1)).toBookingResponseDto(booking);
    }

    @Test
    void addBooking_NonExistentUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.addBooking(999L, bookingRequestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("999");

        verify(userRepository, times(1)).findById(999L);
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void addBooking_NonExistentItem_ShouldThrowNotFoundException() {
        Long nonExistentItemId = 999L;

        BookingRequestDto requestDtoWithNonExistentItem = BookingRequestDto.builder()
                .itemId(nonExistentItemId)
                .start(start)
                .end(end)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(nonExistentItemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.addBooking(1L, requestDtoWithNonExistentItem))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item")
                .hasMessageContaining(nonExistentItemId.toString());

        verify(userRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(nonExistentItemId);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void addBooking_OwnerBookingOwnItem_ShouldThrowException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.addBooking(2L, bookingRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Owner cannot be the same as booker");

        verify(userRepository, times(1)).findById(2L);
        verify(itemRepository, times(1)).findById(10L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void addBooking_ItemNotAvailable_ShouldThrownException() {
        item.setAvailable(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.addBooking(1L, bookingRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item is not available for booking");

        verify(userRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(10L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void updateStatus_Approved_ShouldUpdateToApproved() throws AccessDeniedException {
        booking.setStatus(Status.WAITING);
        when(bookingRepository.findByIdWithItemAndOwner(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.updateStatus(2L, 100L, true);

        assertThat(result).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(Status.APPROVED);

        verify(bookingRepository, times(1)).findByIdWithItemAndOwner(100L);
        verify(bookingRepository, times(1)).save(booking);
        verify(bookingMapper, times(1)).toBookingResponseDto(booking);
    }

    @Test
    void updateStatus_Rejected_ShouldUpdateToRejected() throws AccessDeniedException {
        booking.setStatus(Status.WAITING);
        when(bookingRepository.findByIdWithItemAndOwner(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.updateStatus(2L, 100L, false);

        assertThat(result).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(Status.REJECTED);

        verify(bookingRepository, times(1)).findByIdWithItemAndOwner(100L);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void updateStatus_NonExistentBooking_ShouldThrowNotFoundException() {
        when(bookingRepository.findByIdWithItemAndOwner(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateStatus(2L, 999L, true))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booking")
                .hasMessageContaining("999");

        verify(bookingRepository, times(1)).findByIdWithItemAndOwner(999L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void updateStatus_UserIsNotOwner_ShouldThrowAccessDeniedException() {
        when(bookingRepository.findByIdWithItemAndOwner(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateStatus(999L, 100L, true))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied");

        verify(bookingRepository, times(1)).findByIdWithItemAndOwner(100L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void updateStatus_StatusNotWaiting_ShouldThrowException() {
        booking.setStatus(Status.APPROVED);
        when(bookingRepository.findByIdWithItemAndOwner(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateStatus(2L, 100L, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The reservation has already been processed");

        verify(bookingRepository, times(1)).findByIdWithItemAndOwner(100L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void getById_ForBooker_ShouldReturnBooking() throws AccessDeniedException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByIdWithItemAndBooker(100L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.getById(100L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);

        verify(userRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).findByIdWithItemAndBooker(100L);
        verify(bookingMapper, times(1)).toBookingResponseDto(booking);
    }

    @Test
    void getById_ForOwner_ShouldReturnBooking() throws AccessDeniedException {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByIdWithItemAndBooker(100L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.getById(100L, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);

        verify(userRepository, times(1)).findById(2L);
        verify(bookingRepository, times(1)).findByIdWithItemAndBooker(100L);
    }

    @Test
    void getById_NonExistentUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getById(100L, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("999");

        verify(userRepository, times(1)).findById(999L);
        verify(bookingRepository, never()).findByIdWithItemAndBooker(anyLong());
    }

    @Test
    void getById_NonExistentBooking_ShouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByIdWithItemAndBooker(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getById(999L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booking")
                .hasMessageContaining("999");

        verify(userRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).findByIdWithItemAndBooker(999L);
    }

    @Test
    void getById_UnauthorizedUser_ShouldThrowAccessDeniedException() {
        User otherUser = User.builder().id(3L).build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(otherUser));
        when(bookingRepository.findByIdWithItemAndBooker(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getById(100L, 3L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied");

        verify(userRepository, times(1)).findById(3L);
        verify(bookingRepository, times(1)).findByIdWithItemAndBooker(100L);
        verify(bookingMapper, never()).toBookingResponseDto(any());
    }

    @Test
    void getBookingsByBooker_ShouldReturnSortedBookings() {
        Booking pastBooking = Booking.builder().id(101L).start(now.minusDays(5)).build();
        Booking futureBooking = Booking.builder().id(102L).start(now.plusDays(5)).build();
        List<Booking> bookings = new ArrayList<>(List.of(pastBooking, futureBooking));

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerId(1L)).thenReturn(bookings);
        when(bookingMapper.toBookingResponseDto(any())).thenReturn(bookingResponseDto);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(1L, State.ALL);

        assertThat(result).hasSize(2);
        verify(userRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).findAllByBookerId(1L);
        verify(bookingMapper, times(2)).toBookingResponseDto(any());
    }

    @Test
    void getBookingsByOwner_ShouldReturnSortedBookings() {
        List<Booking> bookings = new ArrayList<>(List.of(
                Booking.builder().id(101L).start(now.minusDays(10)).build(),
                Booking.builder().id(102L).start(now.minusDays(1)).build()
        ));

        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByOwnerId(2L)).thenReturn(bookings);
        when(bookingMapper.toBookingResponseDto(any())).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            return BookingResponseDto.builder().id(b.getId()).build();
        });

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(2L, State.ALL);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(102L);
        assertThat(result.get(1).getId()).isEqualTo(101L);
    }

    @Test
    void getBookingsByBooker_AllStates_ShouldCallCorrectRepositoryMethods() {
        List<Booking> emptyList = new ArrayList<>();

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerId(1L)).thenReturn(emptyList);
        when(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(eq(1L), any(), any())).thenReturn(emptyList);
        when(bookingRepository.findAllByBookerIdAndEndBefore(eq(1L), any())).thenReturn(emptyList);
        when(bookingRepository.findAllByBookerIdAndStartAfter(eq(1L), any())).thenReturn(emptyList);
        when(bookingRepository.findAllByBookerIdAndStatus(1L, Status.WAITING)).thenReturn(emptyList);
        when(bookingRepository.findAllByBookerIdAndStatus(1L, Status.REJECTED)).thenReturn(emptyList);

        bookingService.getBookingsByBooker(1L, State.ALL);
        verify(bookingRepository).findAllByBookerId(1L);

        bookingService.getBookingsByBooker(1L, State.CURRENT);
        verify(bookingRepository).findAllByBookerIdAndStartBeforeAndEndAfter(eq(1L), any(), any());

        bookingService.getBookingsByBooker(1L, State.PAST);
        verify(bookingRepository).findAllByBookerIdAndEndBefore(eq(1L), any());

        bookingService.getBookingsByBooker(1L, State.FUTURE);
        verify(bookingRepository).findAllByBookerIdAndStartAfter(eq(1L), any());

        bookingService.getBookingsByBooker(1L, State.WAITING);
        verify(bookingRepository).findAllByBookerIdAndStatus(1L, Status.WAITING);

        bookingService.getBookingsByBooker(1L, State.REJECTED);
        verify(bookingRepository).findAllByBookerIdAndStatus(1L, Status.REJECTED);

        verify(userRepository, times(6)).findById(1L);
    }

    @Test
    void getBookingsByOwner_AllStates_ShouldCallCorrectRepositoryMethods() {
        List<Booking> emptyList = new ArrayList<>();
        
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByOwnerId(2L)).thenReturn(emptyList);
        when(bookingRepository.findAllByOwnerIdAndStartBeforeAndEndAfter(eq(2L), any())).thenReturn(emptyList);
        when(bookingRepository.findAllByOwnerIdAndEndBefore(eq(2L), any())).thenReturn(emptyList);
        when(bookingRepository.findAllByOwnerIdAndStartAfter(eq(2L), any())).thenReturn(emptyList);
        when(bookingRepository.findAllByOwnerIdAndStatus(2L, Status.WAITING)).thenReturn(emptyList);
        when(bookingRepository.findAllByOwnerIdAndStatus(2L, Status.REJECTED)).thenReturn(emptyList);

        bookingService.getBookingsByOwner(2L, State.ALL);
        verify(bookingRepository).findAllByOwnerId(2L);

        bookingService.getBookingsByOwner(2L, State.CURRENT);
        verify(bookingRepository).findAllByOwnerIdAndStartBeforeAndEndAfter(eq(2L), any());

        bookingService.getBookingsByOwner(2L, State.PAST);
        verify(bookingRepository).findAllByOwnerIdAndEndBefore(eq(2L), any());

        bookingService.getBookingsByOwner(2L, State.FUTURE);
        verify(bookingRepository).findAllByOwnerIdAndStartAfter(eq(2L), any());

        bookingService.getBookingsByOwner(2L, State.WAITING);
        verify(bookingRepository).findAllByOwnerIdAndStatus(2L, Status.WAITING);

        bookingService.getBookingsByOwner(2L, State.REJECTED);
        verify(bookingRepository).findAllByOwnerIdAndStatus(2L, Status.REJECTED);

        verify(userRepository, times(6)).findById(2L);
    }
}