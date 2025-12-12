package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookingRequestDto;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final ru.practicum.shareit.booking.BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addBooking(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @RequestBody @Valid BookingRequestDto bookingRequestDto
    ) {
        log.info("Gateway: POST /bookings - add booking by user {}", userId);
        return bookingClient.addBooking(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateStatus(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @PathVariable @Positive(message = "Booking id must be a positive number") Long bookingId,
            @RequestParam Boolean approved
    ) {
        log.info("Gateway: PATCH /bookings/{} - update status to {} by user {}", bookingId, approved, userId);
        return bookingClient.updateStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @PathVariable @Positive(message = "Booking id must be a positive number") Long bookingId
    ) {
        log.info("Gateway: GET /bookings/{} - get booking by user {}", bookingId, userId);
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByBooker(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @RequestParam(defaultValue = "ALL") String state
    ) {
        log.info("Gateway: GET /bookings - get bookings by booker {}, state: {}", userId, state);
        return bookingClient.getBookingsByBooker(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @RequestParam(defaultValue = "ALL") String state
    ) {
        log.info("Gateway: GET /bookings/owner - get bookings by owner {}, state: {}", userId, state);
        return bookingClient.getBookingsByOwner(userId, state);
    }
}