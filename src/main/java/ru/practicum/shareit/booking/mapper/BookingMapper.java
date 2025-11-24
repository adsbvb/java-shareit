package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {


    BookingResponseDto toBookingResponseDto(Booking booking);

    @Mapping(target = "bookerId", source = "booker.id")
    BookingForItemDto toBookingForItemDto(Booking booking);
}
