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
    public ItemDto createItem(@Valid @RequestBody ItemCreateDto itemCreateDto,
                              @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.createItem(itemCreateDto, ownerId);
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable Long id,
                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemById(id, userId);
    }

    @GetMapping
    public List<ItemDto> getAllItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.getAllItemsByOwnerId(ownerId);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@PathVariable Long id,
                              @Valid @RequestBody ItemUpdateDto itemUpdateDto,
                              @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.updateItem(id, itemUpdateDto, ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchAvailableItems(@RequestParam String text) {
        return itemService.searchAvailableItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(
            @PathVariable Long itemId,
            @Valid @RequestBody CommentRequestDto commentRequestDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.addComment(itemId, commentRequestDto, userId);
    }
}