package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

@Component
public class BookingMapper {

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    public BookingMapper(UserMapper userMapper, ItemMapper itemMapper) {
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
    }

    public Booking toEntity(BookingRequestDto bookingRequestDto) {
        return Booking.builder()
                .startTime(bookingRequestDto.getStart())
                .endTime(bookingRequestDto.getEnd())
                .status(BookingStatus.WAITING)
                .build();
    }

    public BookingResponseDto toDto(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStartTime())
                .end(booking.getEndTime())
                .status(booking.getStatus())
                .booker(userMapper.toDto(booking.getBooker()))
                .item(itemMapper.toDto(booking.getItem()))
                .build();
    }

    public BookingShortDto toShortDto(Booking booking) {
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStartTime())
                .end(booking.getEndTime())
                .build();
    }
}