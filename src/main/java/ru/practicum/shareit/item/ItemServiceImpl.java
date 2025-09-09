package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + ownerId));

        Item item = itemMapper.toEntity(itemDto, owner);
        Item savedItem = itemRepository.save(item);
        return itemMapper.toDto(savedItem);
    }

    @Override
    public ItemDto getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Item not found with id: " + id));
        return itemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> getAllItemsByOwnerId(Long ownerId) {
        return itemRepository.findByOwnerId(ownerId).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto updateItem(Long id, ItemDto itemDto, Long ownerId) {
        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Item not found with id: " + id));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new SecurityException("Only owner can update the item");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setIsAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.update(existingItem);
        return itemMapper.toDto(updatedItem);
    }

    @Override
    public List<ItemDto> searchAvailableItems(String text) {
        return itemRepository.searchAvailableByText(text).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }
}