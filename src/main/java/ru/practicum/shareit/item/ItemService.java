package ru.practicum.shareit.item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemCreateDto itemCreateDto, Long ownerId);

    ItemDto getItemById(Long id);

    List<ItemDto> getAllItemsByOwnerId(Long ownerId);

    ItemDto updateItem(Long id, ItemUpdateDto itemUpdateDto, Long ownerId);

    List<ItemDto> searchAvailableItems(String text);
}