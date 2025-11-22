package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentCreateDto {
    @NotBlank(message = "Comment text cannot be empty")
    @Size(max = 1000, message = "Comment text must not exceed 1000 characters")
    private String text;
}