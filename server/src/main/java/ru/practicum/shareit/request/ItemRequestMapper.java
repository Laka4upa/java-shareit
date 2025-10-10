package ru.practicum.shareit.request;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.Collections;

@Component
public class ItemRequestMapper {

    private final ItemMapper itemMapper;

    public ItemRequestMapper(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public ItemRequest toEntity(ItemRequestDto requestDto) {
        return ItemRequest.builder()
                .description(requestDto.getDescription())
                .created(LocalDateTime.now())
                .build();
    }

    public ItemRequestResponseDto toDto(ItemRequest itemRequest) {
        return ItemRequestResponseDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(itemRequest.getItems() != null ?
                        itemRequest.getItems().stream().map(itemMapper::toDto).toList() :
                        Collections.emptyList())
                .build();
    }
}