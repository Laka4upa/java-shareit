package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemMapperTest {

    @InjectMocks
    private ItemMapper itemMapper;

    @Test
    void toDto_WithValidItem_ShouldReturnItemDto() {
        // Given
        User owner = User.builder()
                .id(1L)
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

        // When
        ItemDto dto = itemMapper.toDto(item);

        // Then
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Item", dto.getName());
        assertEquals("Description", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertNull(dto.getRequestId()); // No request associated
    }

    @Test
    void toDto_WithItemWithRequest_ShouldReturnItemDtoWithRequestId() {
        // Given
        User owner = User.builder().id(1L).build();
        ru.practicum.shareit.request.ItemRequest request = ru.practicum.shareit.request.ItemRequest.builder()
                .id(10L)
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        // When
        ItemDto dto = itemMapper.toDto(item);

        // Then
        assertNotNull(dto);
        assertEquals(10L, dto.getRequestId());
    }

    @Test
    void toEntity_WithValidDtoAndOwner_ShouldReturnItem() {
        // Given
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .build();

        // When
        Item item = itemMapper.toEntity(dto, owner);

        // Then
        assertNotNull(item);
        assertEquals(1L, item.getId());
        assertEquals("Item", item.getName());
        assertEquals("Description", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(owner, item.getOwner());
    }
}