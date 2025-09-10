package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class User {
    private Long id;
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
    @Email(message = "Некорректный формат email")
    @NotBlank(message = "Почта не может быть пустой")
    private String email;
}
