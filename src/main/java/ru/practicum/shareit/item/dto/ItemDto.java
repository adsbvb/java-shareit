package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.error.validation.CreateGroup;
import ru.practicum.shareit.error.validation.PatchUpdateGroup;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long id;

    @NotBlank(groups = CreateGroup.class, message = "Name cannot be empty")
    @Size(groups = {CreateGroup.class, PatchUpdateGroup.class}, max = 255, message = "Name must not exceed 255 characters")
    String name;

    @NotBlank(groups = CreateGroup.class, message = "Description cannot be empty")
    @Size(groups = {CreateGroup.class, PatchUpdateGroup.class}, max = 1000, message = "Description must not exceed 1000 characters")
    String description;

    @NotNull(groups = CreateGroup.class, message = "Available cannot be null")
    Boolean available;

    @Positive(groups = {CreateGroup.class, PatchUpdateGroup.class}, message = "Item request id must be positive")
    Long itemRequest;
}
