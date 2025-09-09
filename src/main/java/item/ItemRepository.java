package item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);

    Optional<Item> findById(Long id);

    List<Item> findAll();

    List<Item> findByOwnerId(Long ownerId);

    List<Item> searchAvailableByText(String text);

    Item update(Item item);

    void deleteById(Long id);
}