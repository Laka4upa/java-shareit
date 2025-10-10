package ru.practicum.shareit.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 512)
    @Email(message = "Некорректный формат email")
    @NotBlank(message = "Почта не может быть пустой")
    private String email;
}