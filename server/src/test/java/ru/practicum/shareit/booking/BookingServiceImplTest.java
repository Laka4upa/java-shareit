package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createBooking_WithValidData_ShouldReturnBooking() {
        Long bookerId = 2L;
        Long itemId = 1L;

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        User booker = User.builder().id(bookerId).build();
        User owner = User.builder().id(1L).build();
        Item item = Item.builder()
                .id(itemId)
                .available(true)
                .owner(owner)
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();

        BookingResponseDto expectedResponse = BookingResponseDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingMapper.toEntity(requestDto)).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(expectedResponse);
        when(bookingRepository.existsOverlappingBookings(anyLong(), any(), any())).thenReturn(false);

        BookingResponseDto result = bookingService.createBooking(requestDto, bookerId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_WhenItemNotAvailable_ShouldThrowValidationException() {
        Long bookerId = 2L;
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        User booker = User.builder().id(bookerId).build();
        User owner = User.builder().id(1L).build();
        Item item = Item.builder()
                .id(1L)
                .available(false)
                .owner(owner)
                .build();

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(requestDto, bookerId));
    }

    @Test
    void createBooking_WhenOwnerBooksOwnItem_ShouldThrowConflictException() {
        Long ownerId = 1L;
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        User owner = User.builder().id(ownerId).build();
        Item item = Item.builder()
                .id(1L)
                .available(true)
                .owner(owner)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ConflictException.class,
                () -> bookingService.createBooking(requestDto, ownerId));
    }

    @Test
    void approveBooking_WhenUserNotOwner_ShouldThrowForbiddenException() {
        Long bookingId = 1L;
        Long ownerId = 1L;
        Long anotherUserId = 2L;

        User owner = User.builder().id(ownerId).build();
        User booker = User.builder().id(3L).build();
        Item item = Item.builder().id(1L).owner(owner).build();
        Booking booking = Booking.builder()
                .id(bookingId)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(ForbiddenException.class,
                () -> bookingService.approveBooking(bookingId, true, anotherUserId));
    }

    @Test
    void getBookingById_WhenUserNotOwnerOrBooker_ShouldThrowException() {
        Long bookingId = 1L;
        Long userId = 1L;
        Long anotherUserId = 4L;

        User owner = User.builder().id(2L).build();
        User booker = User.builder().id(3L).build();
        Item item = Item.builder().id(1L).owner(owner).build();
        Booking booking = Booking.builder()
                .id(bookingId)
                .item(item)
                .booker(booker)
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(Exception.class,
                () -> bookingService.getBookingById(bookingId, anotherUserId));
    }
}