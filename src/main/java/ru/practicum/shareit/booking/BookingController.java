package ru.practicum.shareit.booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto add(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @RequestBody BookingRequestDto bookingRequestDto
    ) {
        log.debug("Add booking request received");
        return bookingService.addBooking(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateStatus(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @PathVariable(name = "bookingId") @Positive(message = "Booking id must be a positive number") Long bookingId,
            @RequestParam @NotNull Boolean approved
    ) throws AccessDeniedException {
        log.debug("Update booking status request received");
        return bookingService.updateStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @PathVariable(name = "bookingId") @Positive(message = "Booking id must be a positive number") Long bookingId
    ) throws AccessDeniedException {
        log.debug("Get booking request received");
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getBookingsByBooker(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @RequestParam(defaultValue = "ALL") State state
    ) {
        log.debug("Get booking request received by booker");
        return bookingService.getBookingsByBooker(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsByOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @RequestParam(defaultValue = "ALL") State state
    ) {
        log.debug("Get booking request received by owner");
        return bookingService.getBookingsByOwner(userId, state);
    }
}
