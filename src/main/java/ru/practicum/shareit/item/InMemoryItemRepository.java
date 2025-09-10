package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(counter.getAndIncrement());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchAvailableByText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String searchText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> {
                    boolean nameMatches = item.getName() != null &&
                            item.getName().toLowerCase().contains(searchText);
                    boolean descMatches = item.getDescription() != null &&
                            item.getDescription().toLowerCase().contains(searchText);
                    return nameMatches || descMatches;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Item update(Item item) {
        if (items.containsKey(item.getId())) {
            items.put(item.getId(), item);
            return item;
        }
        throw new NoSuchElementException("Предмет не найден с id: " + item.getId());
    }

    @Override
    public void deleteById(Long id) {
        items.remove(id);
    }
}