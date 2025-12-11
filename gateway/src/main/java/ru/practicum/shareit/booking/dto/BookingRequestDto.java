package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDto {
    @NotNull(message = "Item id must be specified")
    @Positive(message = "Item id must be a positive number")
    private Long itemId;

    @NotNull(message = "Start date must be specified")
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDateTime start;

    @NotNull(message = "End date must be specified")
    private LocalDateTime end;

    public LocalDateTime getEnd() {
        if (end != null && start != null && !end.isAfter(start)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        return end;
    }
}