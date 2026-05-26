package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private Long id;
    @Email(message = "Email должен быть в корректном формате")
    private String email;
    private String name;

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasEmail() {
        return !(email == null || email.isBlank());
    }
}
