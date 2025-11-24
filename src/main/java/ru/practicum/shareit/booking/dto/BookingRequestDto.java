package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class BookingRequestDto {

    @NotNull(message = "Item id must be specified")
    @Positive(message = "Item id must be a positive number")
    Long itemId;

    @NotNull(message = "Start date must be specified")
    @FutureOrPresent(message = "Start date cannot be in the past")
    LocalDateTime start;

    @NotNull(message = "End date must be specified")
    @Future(message = "End date cannot be in the past")
    LocalDateTime end;
}
