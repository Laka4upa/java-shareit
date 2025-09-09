package ru.practicum.shareit.item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto getItemById(Long id);

    List<ItemDto> getAllItemsByOwnerId(Long ownerId);

    ItemDto updateItem(Long id, ItemDto itemDto, Long ownerId);

    List<ItemDto> searchAvailableItems(String text);
}