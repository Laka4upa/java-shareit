package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long bookerId);

    BookingResponseDto approveBooking(Long bookingId, Boolean approved, Long ownerId);

    BookingResponseDto getBookingById(Long bookingId, Long userId);

    List<BookingResponseDto> getUserBookings(Long userId, BookingState state, Pageable pageable);

    List<BookingResponseDto> getOwnerBookings(Long ownerId, BookingState state, Pageable pageable);
}