package ru.practicum.shareit.item;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.shareit.user.User;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank(message = "Название предмета не может быть пустым")
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Описание предмета не может быть пустым")
    private String description;

    @Column(name = "is_available", nullable = false)
    @NotNull(message = "Статус доступности обязателен")
    private Boolean available;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @NotNull(message = "Владелец обязателен")
    private User owner;

    @Column(name = "request_id")
    private Long requestId;
}