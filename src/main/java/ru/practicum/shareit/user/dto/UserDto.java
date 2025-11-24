package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.error.validation.CreateGroup;
import ru.practicum.shareit.error.validation.PatchUpdateGroup;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long id;

    @NotBlank(groups = {CreateGroup.class}, message = "Name cannot be empty")
    @Size(max = 255, groups = {CreateGroup.class, PatchUpdateGroup.class}, message = "Name must not exceed 255 characters")
    String name;

    @NotBlank(groups = {CreateGroup.class}, message = "Email cannot be empty")
    @Email(groups = {CreateGroup.class, PatchUpdateGroup.class}, message = "Invalid email format")
    @Size(max = 512, groups = {CreateGroup.class, PatchUpdateGroup.class}, message = "Email must not exceed 512 characters")
    String email;
}
