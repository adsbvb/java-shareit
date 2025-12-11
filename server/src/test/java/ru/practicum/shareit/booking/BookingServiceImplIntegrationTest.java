package ru.practicum.shareit.booking;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("BookingServiceImpl.updateStatus() integration tests")
public class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private User anotherUser;
    private Item item;
    private Booking waitingBooking;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        now = LocalDateTime.now();

        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@example.com")
                .build());

        anotherUser = userRepository.save(User.builder()
                .name("Another User")
                .email("another@example.com")
                .build());

        item = itemRepository.save(Item.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(owner)
                .build());

        waitingBooking = bookingRepository.save(Booking.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .status(Status.WAITING)
                .booker(booker)
                .item(item)
                .build());
    }

    @Test
    void updateStatus_ApprovedTrue_ShouldUpdateStatusToApproved() throws AccessDeniedException {
        Long bookingId = waitingBooking.getId();

        BookingResponseDto result = bookingService.updateStatus(owner.getId(), bookingId, true);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bookingId);
        assertThat(result.getStatus()).isEqualTo(Status.APPROVED);

        Booking updatedBooking = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(updatedBooking.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    void updateStatus_ApprovedFalse_ShouldUpdateStatusToRejected() throws AccessDeniedException {
        Long bookingId = waitingBooking.getId();

        BookingResponseDto result = bookingService.updateStatus(owner.getId(), bookingId, false);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bookingId);
        assertThat(result.getStatus()).isEqualTo(Status.REJECTED);

        Booking updatedBooking = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(updatedBooking.getStatus()).isEqualTo(Status.REJECTED);
    }

    @Test
    void updateStatus_UserIsNotOwner_ShouldThrowAccessDeniedException() {
        Long bookingId = waitingBooking.getId();

        assertThrows(AccessDeniedException.class, () -> {
            bookingService.updateStatus(anotherUser.getId(), bookingId, true);
        });

        assertThrows(AccessDeniedException.class, () -> {
            bookingService.updateStatus(booker.getId(), bookingId, true);
        });

        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(booking.getStatus()).isEqualTo(Status.WAITING);
    }

    @Test
    void updateStatus_NonExistentBooking_ShouldThrowNotFoundException() {
        Long nonExistentBookingId = 9999L;

        assertThrows(NotFoundException.class, () -> {
            bookingService.updateStatus(owner.getId(), nonExistentBookingId, true);
        });
    }

    @Test
    void updateStatus_StatusIsNotWaiting_ShouldThrowValidationException() throws AccessDeniedException {
        Booking approvedBooking = bookingRepository.save(Booking.builder()
                .start(now.plusDays(5))
                .end(now.plusDays(7))
                .status(Status.APPROVED)
                .booker(booker)
                .item(item)
                .build());

        assertThrows(ValidationException.class, () -> {
            bookingService.updateStatus(owner.getId(), approvedBooking.getId(), false);
        });

        Booking rejectedBooking = bookingRepository.save(Booking.builder()
                .start(now.plusDays(8))
                .end(now.plusDays(10))
                .status(Status.REJECTED)
                .booker(booker)
                .item(item)
                .build());

        assertThrows(ValidationException.class, () -> {
            bookingService.updateStatus(owner.getId(), rejectedBooking.getId(), true);
        });

        try {
            bookingService.updateStatus(owner.getId(), approvedBooking.getId(), true);
        } catch (ValidationException e) {
            assertThat(e.getMessage()).contains("The reservation has already been processed");
            assertThat(e.getMessage()).contains("Current status: " + Status.APPROVED);
        }
    }
}