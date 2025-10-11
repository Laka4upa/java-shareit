package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestMapperTest {

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestMapper itemRequestMapper;

    @Test
    void toEntity_WithValidDto_ShouldReturnItemRequest() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("I need a drill")
                .build();

        ItemRequest request = itemRequestMapper.toEntity(dto);

        assertNotNull(request);
        assertEquals("I need a drill", request.getDescription());
        assertNotNull(request.getCreated());
    }

    @Test
    void toDto_WithValidRequest_ShouldReturnResponseDto() {
        User requester = User.builder()
                .id(1L)
                .name("Requester")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("I need a drill")
                .requester(requester)
                .created(LocalDateTime.of(2024, 1, 1, 10, 0))
                .items(List.of(item))
                .build();

        when(itemMapper.toDto(any(Item.class))).thenReturn(
                ru.practicum.shareit.item.dto.ItemDto.builder()
                        .id(1L)
                        .name("Drill")
                        .description("Powerful drill")
                        .available(true)
                        .build());

        ItemRequestResponseDto dto = itemRequestMapper.toDto(request);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("I need a drill", dto.getDescription());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), dto.getCreated());
        assertFalse(dto.getItems().isEmpty());
        assertEquals("Drill", dto.getItems().get(0).getName());
    }

    @Test
    void toDto_WithRequestWithoutItems_ShouldReturnEmptyItemsList() {
        User requester = User.builder().id(1L).build();

        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("I need a drill")
                .requester(requester)
                .created(LocalDateTime.now())
                .items(null) // No items
                .build();

        ItemRequestResponseDto dto = itemRequestMapper.toDto(request);

        assertNotNull(dto);
        assertTrue(dto.getItems().isEmpty());
    }
}