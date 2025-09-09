package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@Valid @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.createItem(itemDto, ownerId);
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable Long id) {
        return itemService.getItemById(id);
    }

    @GetMapping
    public List<ItemDto> getAllItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.getAllItemsByOwnerId(ownerId);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@PathVariable Long id,
                              @Valid @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.updateItem(id, itemDto, ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchAvailableItems(@RequestParam String text) {
        return itemService.searchAvailableItems(text);
    }
}