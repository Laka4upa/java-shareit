package ru.practicum.shareit.booking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
public class Booking {
    private Long id;
    @NotNull(message = "Дата начала бронирования обязательна")
    @Future(message = "Дата начала должна быть в будущем")
    private LocalDateTime startTime;
    @NotNull(message = "Дата окончания бронирования обязательна")
    @Future(message = "Дата окончания должна быть в будущем")
    private LocalDateTime endTime;
    @NotNull(message = "Предмет обязателен")
    private Item item;
    @NotNull(message = "Заказчик обязателен")
    private User booker;
    @NotNull(message = "Статус бронирования обязателен")
    private BookingStatus status;
}