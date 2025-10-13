package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void createRequest_ShouldReturnRequest() throws Exception {
        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.createRequest(any(ItemRequestDto.class), anyLong()))
                .thenReturn(responseDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"Нужна дрель\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }

    @Test
    void getUserRequests_ShouldReturnList() throws Exception {
        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.getUserRequests(anyLong()))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"));
    }

    @Test
    void getAllRequests_ShouldReturnList() throws Exception {
        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.getAllRequests(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"));
    }

    @Test
    void getAllRequests_WithDefaultParams_ShouldWork() throws Exception {
        when(itemRequestService.getAllRequests(anyLong(), any(Pageable.class)))
                .thenReturn(List.of());

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_WithNegativeFrom_ShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequests_WithZeroSize_ShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequests_WithNegativeSize_ShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "-5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestById_ShouldReturnRequest() throws Exception {
        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenReturn(responseDto);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }

    @Test
    void createRequest_WithEmptyDescription_ShouldReturnBadRequest() throws Exception {
        when(itemRequestService.createRequest(any(ItemRequestDto.class), anyLong()))
                .thenThrow(new ValidationException("Описание не может быть пустым"));

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"\"}"))
                .andExpect(status().isBadRequest());
    }
}