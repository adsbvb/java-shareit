package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto add(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody BookingRequestDto bookingRequestDto
    ) {
        log.info("Server: POST /bookings - add booking by user {}", userId);
        return bookingService.addBooking(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateStatus(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable(name = "bookingId") Long bookingId,
            @RequestParam Boolean approved
    ) throws AccessDeniedException {
        log.info("Server: PATCH /bookings/{} - update status to {} by user {}", bookingId, approved, userId);
        return bookingService.updateStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable(name = "bookingId") Long bookingId
    ) throws AccessDeniedException {
        log.info("Server: GET /bookings/{} - by user {}", bookingId, userId);
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getBookingsByBooker(
            @RequestHeader("X-Sharer-User-Id")  Long userId,
            @RequestParam(defaultValue = "ALL") State state
    ) {
        log.info("Server: GET /bookings/{} - by user {}", state, userId);
        return bookingService.getBookingsByBooker(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") State state
    ) {
        log.info("Server: GET /bookings/{} - by owner {}", state, userId);
        return bookingService.getBookingsByOwner(userId, state);
    }
}
