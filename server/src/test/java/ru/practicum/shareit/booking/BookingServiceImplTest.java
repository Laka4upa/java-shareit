package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void createBooking_WhenUserNotFound_ShouldThrowException() {
        BookingRequestDto requestDto = new BookingRequestDto(1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2));

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                bookingService.createBooking(requestDto, 1L));
    }

    @Test
    void createBooking_WhenItemNotFound_ShouldThrowException() {
        BookingRequestDto requestDto = new BookingRequestDto(1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                bookingService.createBooking(requestDto, 1L));
    }

    @Test
    void createBooking_WhenItemNotAvailable_ShouldThrowValidationException() {
        User user = new User(2L, "user", "user@mail.ru");
        User owner = new User(1L, "owner", "owner@mail.ru");
        Item item = new Item(1L, "item", "desc", false, owner, null);
        BookingRequestDto requestDto = new BookingRequestDto(1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2));

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(requestDto, 2L));
    }

    @Test
    void createBooking_WhenOwnerBooksOwnItem_ShouldThrowConflictException() {
        User owner = new User(1L, "owner", "owner@mail.ru");
        Item item = new Item(1L, "item", "desc", true, owner, null);
        BookingRequestDto requestDto = new BookingRequestDto(1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ConflictException.class, () ->
                bookingService.createBooking(requestDto, 1L));
    }

    @Test
    void createBooking_WhenStartAfterEnd_ShouldThrowValidationException() {
        User user = new User(2L, "user", "user@mail.ru");
        User owner = new User(1L, "owner", "owner@mail.ru");
        Item item = new Item(1L, "item", "desc", true, owner, null);
        BookingRequestDto requestDto = new BookingRequestDto(1L,
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(1));

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(requestDto, 2L));
    }

    @Test
    void createBooking_WhenStartInPast_ShouldThrowValidationException() {
        User user = new User(2L, "user", "user@mail.ru");
        User owner = new User(1L, "owner", "owner@mail.ru");
        Item item = new Item(1L, "item", "desc", true, owner, null);
        BookingRequestDto requestDto = new BookingRequestDto(1L,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1));

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(requestDto, 2L));
    }

    @Test
    void createBooking_WhenOverlappingExists_ShouldThrowValidationException() {
        User user = new User(2L, "user", "user@mail.ru");
        User owner = new User(1L, "owner", "owner@mail.ru");
        Item item = new Item(1L, "item", "desc", true, owner, null);
        BookingRequestDto requestDto = new BookingRequestDto(1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2));

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsOverlappingBookings(1L, requestDto.getStart(), requestDto.getEnd()))
                .thenReturn(true);

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(requestDto, 2L));
    }

    @Test
    void approveBooking_WhenBookingNotFound_ShouldThrowException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                bookingService.approveBooking(1L, true, 1L));
    }

    @Test
    void approveBooking_WhenStatusNotWaiting_ShouldThrowConflictException() {
        User owner = new User(1L, "owner", "owner@mail.ru");
        Item item = new Item(1L, "item", "desc", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2), item, owner, BookingStatus.APPROVED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ConflictException.class, () ->
                bookingService.approveBooking(1L, true, 1L));
    }

    @Test
    void approveBooking_WhenApprovedAndOverlappingExists_ShouldThrowValidationException() {
        User owner = new User(1L, "owner", "owner@mail.ru");
        User booker = new User(2L, "booker", "booker@mail.ru");
        Item item = new Item(1L, "item", "desc", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2), item, booker, BookingStatus.WAITING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.existsOverlappingBookingsExcluding(1L,
                booking.getStartTime(), booking.getEndTime(), 1L))
                .thenReturn(true);

        assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(1L, true, 1L));
    }

    @Test
    void approveBooking_WhenRejected_ShouldUpdateStatus() {
        User owner = new User(1L, "owner", "owner@mail.ru");
        User booker = new User(2L, "booker", "booker@mail.ru");
        Item item = new Item(1L, "item", "desc", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2), item, booker, BookingStatus.WAITING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponseDto responseDto = BookingResponseDto.builder()
                .status(BookingStatus.REJECTED)
                .build();
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(responseDto);

        BookingResponseDto result = bookingService.approveBooking(1L, false, 1L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void getBookingById_WhenUserNotBookerOrOwner_ShouldThrowException() {
        User owner = new User(1L, "owner", "owner@mail.ru");
        User booker = new User(2L, "booker", "booker@mail.ru");
        User otherUser = new User(3L, "other", "other@mail.ru");
        Item item = new Item(1L, "item", "desc", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2), item, booker, BookingStatus.WAITING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(NoSuchElementException.class, () ->
                bookingService.getBookingById(1L, 3L));
    }

    @Test
    void getUserBookings_WithAllStates_ShouldWorkCorrectly() {
        User user = new User(1L, "user", "user@mail.ru");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(bookingRepository.findByBookerIdOrderByStartTimeDesc(anyLong(), any())).thenReturn(List.of());
        when(bookingRepository.findByBookerIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(anyLong(), any(), any(), any())).thenReturn(List.of());
        when(bookingRepository.findByBookerIdAndEndTimeBeforeOrderByStartTimeDesc(anyLong(), any(), any())).thenReturn(List.of());
        when(bookingRepository.findByBookerIdAndStartTimeAfterOrderByStartTimeDesc(anyLong(), any(), any())).thenReturn(List.of());
        when(bookingRepository.findByBookerIdAndStatusOrderByStartTimeDesc(anyLong(), any(), any())).thenReturn(List.of());

        // Тестируем все валидные состояния
        for (BookingState state : BookingState.values()) {
            assertDoesNotThrow(() ->
                    bookingService.getUserBookings(1L, state, PageRequest.of(0, 10))
            );
        }
    }

    @Test
    void getOwnerBookings_WithAllStates_ShouldWorkCorrectly() {
        User user = new User(1L, "user", "user@mail.ru");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(bookingRepository.findByItemOwnerIdOrderByStartTimeDesc(anyLong(), any())).thenReturn(List.of());
        when(bookingRepository.findByItemOwnerIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(anyLong(), any(), any(), any())).thenReturn(List.of());
        when(bookingRepository.findByItemOwnerIdAndEndTimeBeforeOrderByStartTimeDesc(anyLong(), any(), any())).thenReturn(List.of());
        when(bookingRepository.findByItemOwnerIdAndStartTimeAfterOrderByStartTimeDesc(anyLong(), any(), any())).thenReturn(List.of());
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartTimeDesc(anyLong(), any(), any())).thenReturn(List.of());

        for (BookingState state : BookingState.values()) {
            assertDoesNotThrow(() ->
                    bookingService.getOwnerBookings(1L, state, PageRequest.of(0, 10))
            );
        }
    }
}