package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingControllerIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Test
    void createBooking_WithValidData_ShouldCreateBooking() {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        UserDto booker = userService.createUser(UserDto.builder()
                .name("Booker")
                .email("booker@mail.com")
                .build());

        ItemDto item = itemService.createItem(ItemCreateDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build(), owner.getId(), null);

        BookingRequestDto bookingRequest = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingResponseDto result = bookingService.createBooking(bookingRequest, booker.getId());

        assertNotNull(result.getId());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals("WAITING", result.getStatus().name());
    }

    @Test
    void getUserBookings_ShouldReturnUserBookings() {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        UserDto booker = userService.createUser(UserDto.builder()
                .name("Booker")
                .email("booker@mail.com")
                .build());

        ItemDto item = itemService.createItem(ItemCreateDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build(), owner.getId(), null);

        BookingRequestDto bookingRequest = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        bookingService.createBooking(bookingRequest, booker.getId());

        List<BookingResponseDto> bookings = bookingService.getUserBookings(
                booker.getId(), BookingState.ALL, org.springframework.data.domain.PageRequest.of(0, 10));

        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
        assertEquals(item.getId(), bookings.get(0).getItem().getId());
    }

    @Test
    void approveBooking_ShouldChangeStatusToApproved() {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        UserDto booker = userService.createUser(UserDto.builder()
                .name("Booker")
                .email("booker@mail.com")
                .build());

        ItemDto item = itemService.createItem(ItemCreateDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build(), owner.getId(), null);

        BookingRequestDto bookingRequest = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingResponseDto booking = bookingService.createBooking(bookingRequest, booker.getId());

        BookingResponseDto approvedBooking = bookingService.approveBooking(
                booking.getId(), true, owner.getId());

        assertEquals("APPROVED", approvedBooking.getStatus().name());
    }
}