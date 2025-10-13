package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingMapperTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private BookingMapper bookingMapper;

    @Test
    void toEntity_WithValidDto_ShouldReturnBooking() {
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2024, 1, 1, 10, 0))
                .end(LocalDateTime.of(2024, 1, 2, 10, 0))
                .build();

        Booking booking = bookingMapper.toEntity(dto);

        assertNotNull(booking);
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), booking.getStartTime());
        assertEquals(LocalDateTime.of(2024, 1, 2, 10, 0), booking.getEndTime());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
    }

    @Test
    void toDto_WithValidBooking_ShouldReturnResponseDto() {
        User booker = User.builder()
                .id(1L)
                .name("Booker")
                .email("booker@mail.com")
                .build();

        User owner = User.builder()
                .id(2L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .startTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                .endTime(LocalDateTime.of(2024, 1, 2, 10, 0))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        when(userMapper.toDto(any(User.class))).thenReturn(
                ru.practicum.shareit.user.dto.UserDto.builder()
                        .id(1L)
                        .name("Booker")
                        .email("booker@mail.com")
                        .build());

        when(itemMapper.toDto(any(Item.class))).thenReturn(
                ru.practicum.shareit.item.dto.ItemDto.builder()
                        .id(1L)
                        .name("Item")
                        .description("Description")
                        .available(true)
                        .build());

        BookingResponseDto dto = bookingMapper.toDto(booking);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), dto.getStart());
        assertEquals(LocalDateTime.of(2024, 1, 2, 10, 0), dto.getEnd());
        assertEquals(BookingStatus.WAITING, dto.getStatus());
        assertEquals(1L, dto.getBooker().getId());
        assertEquals(1L, dto.getItem().getId());
    }

    @Test
    void toShortDto_WithValidBooking_ShouldReturnShortDto() {
        User booker = User.builder().id(1L).build();

        Booking booking = Booking.builder()
                .id(1L)
                .startTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                .endTime(LocalDateTime.of(2024, 1, 2, 10, 0))
                .booker(booker)
                .build();

        BookingShortDto dto = bookingMapper.toShortDto(booking);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getBookerId());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), dto.getStart());
        assertEquals(LocalDateTime.of(2024, 1, 2, 10, 0), dto.getEnd());
    }
}