package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemRequestDto {
    @NotBlank(message = "Request description cannot be empty")
    @Size(max = 1000, message = "Request description must not exceed 1000 characters")
    private String description;
}
