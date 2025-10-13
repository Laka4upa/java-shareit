package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBooking_ShouldReturnBooking() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();

        when(bookingService.createBooking(any(BookingRequestDto.class), anyLong()))
                .thenReturn(responseDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\": 1, \"start\": \"2024-01-01T10:00:00\", \"end\": \"2024-01-02T10:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approveBooking_ShouldReturnUpdatedBooking() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.approveBooking(anyLong(), anyBoolean(), anyLong()))
                .thenReturn(responseDto);

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingById_ShouldReturnBooking() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(responseDto);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getUserBookings_ShouldReturnList() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.getUserBookings(anyLong(), any(BookingState.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getUserBookings_WithDefaultParams_ShouldWork() throws Exception {
        when(bookingService.getUserBookings(anyLong(), any(BookingState.class), any(Pageable.class)))
                .thenReturn(List.of());

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_ShouldReturnList() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.getOwnerBookings(anyLong(), any(BookingState.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "CURRENT")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getOwnerBookings_WithDefaultParams_ShouldWork() throws Exception {
        when(bookingService.getOwnerBookings(anyLong(), any(BookingState.class), any(Pageable.class)))
                .thenReturn(List.of());

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_WithoutUserId_ShouldReturnInternalError() throws Exception {
        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\": 1, \"start\": \"2024-01-01T10:00:00\", \"end\": \"2024-01-02T10:00:00\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createBooking_WithInvalidDates_ShouldReturnBadRequest() throws Exception {
        when(bookingService.createBooking(any(BookingRequestDto.class), anyLong()))
                .thenThrow(new ValidationException("Дата начала не может быть позже даты окончания"));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\": 1, \"start\": \"2024-01-02T10:00:00\", \"end\": \"2024-01-01T10:00:00\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserBookings_WithInvalidState_ShouldReturnConflict() throws Exception {
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "INVALID"))
                .andExpect(status().isConflict()); // Ожидаем 409 вместо 400
    }
}
