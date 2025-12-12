package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateDto {
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Email(message = "Invalid email format")
    @Size(max = 512, message = "Email must not exceed 512 characters")
    private String email;
}
