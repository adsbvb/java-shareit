package ru.practicum.shareit.item.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentCreateDto {
    private String text;
}