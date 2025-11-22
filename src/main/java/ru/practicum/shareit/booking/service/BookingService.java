package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.State;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface BookingService {
    BookingResponseDto addBooking(Long bookerId, BookingRequestDto createDto);

    BookingResponseDto updateStatus(Long bookerId, Long bookingId, Boolean approved) throws AccessDeniedException;

    BookingResponseDto getById(Long userId, Long bookingId) throws AccessDeniedException;

    List<BookingResponseDto> getBookingsByBooker(Long bookerId, State state);

    List<BookingResponseDto> getBookingsByOwner(Long ownerId, State state);
}
