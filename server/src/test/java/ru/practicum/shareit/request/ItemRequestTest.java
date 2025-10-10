package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestTest {

    @Test
    void equals_ShouldReturnTrue_ForSameObject() {
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        assertTrue(request.equals(request));
    }

    @Test
    void equals_ShouldReturnTrue_ForSameId() {
        ItemRequest request1 = ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        ItemRequest request2 = ItemRequest.builder()
                .id(1L)
                .description("Нужна отвертка") // разное описание
                .created(LocalDateTime.now().plusHours(1)) // разное время
                .build();

        assertTrue(request1.equals(request2));
        assertTrue(request2.equals(request1));
    }

    @Test
    void equals_ShouldReturnFalse_ForDifferentIds() {
        ItemRequest request1 = ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        ItemRequest request2 = ItemRequest.builder()
                .id(2L) // разный ID
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        assertFalse(request1.equals(request2));
        assertFalse(request2.equals(request1));
    }

    @Test
    void equals_ShouldReturnFalse_ForNullId() {
        ItemRequest request1 = ItemRequest.builder()
                .id(null) // null ID
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        ItemRequest request2 = ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        assertFalse(request1.equals(request2));
        assertFalse(request2.equals(request1));
    }

    @Test
    void equals_ShouldReturnFalse_ForNullObject() {
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        assertFalse(request.equals(null));
    }

    @Test
    void equals_ShouldReturnFalse_ForDifferentClass() {
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        String differentClass = "not an ItemRequest";

        assertFalse(request.equals(differentClass));
    }

    @Test
    void hashCode_ShouldReturnSameValue_ForSameClass() {
        ItemRequest request1 = ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        ItemRequest request2 = ItemRequest.builder()
                .id(2L) // разный ID, но hashCode основан на классе
                .description("Нужна отвертка")
                .created(LocalDateTime.now().plusHours(1))
                .build();

        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void hashCode_ShouldReturnClassHashCode() {
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        int expectedHashCode = ItemRequest.class.hashCode();
        assertEquals(expectedHashCode, request.hashCode());
    }

    @Test
    void builder_ShouldCreateObjectWithAllFields() {
        User user = new User(1L, "user", "user@mail.ru");
        LocalDateTime created = LocalDateTime.now();
        List<Item> items = List.of();

        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель")
                .requester(user)
                .created(created)
                .items(items)
                .build();

        assertEquals(1L, request.getId());
        assertEquals("Нужна дрель", request.getDescription());
        assertEquals(user, request.getRequester());
        assertEquals(created, request.getCreated());
        assertEquals(items, request.getItems());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyObject() {
        ItemRequest request = new ItemRequest();

        assertNull(request.getId());
        assertNull(request.getDescription());
        assertNull(request.getRequester());
        assertNull(request.getCreated());
        assertNotNull(request.getItems());    }

    @Test
    void allArgsConstructor_ShouldCreateObjectWithAllFields() {
        User user = new User(1L, "user", "user@mail.ru");
        LocalDateTime created = LocalDateTime.now();
        List<Item> items = List.of();

        ItemRequest request = new ItemRequest(1L, "Нужна дрель", user, created, items);

        assertEquals(1L, request.getId());
        assertEquals("Нужна дрель", request.getDescription());
        assertEquals(user, request.getRequester());
        assertEquals(created, request.getCreated());
        assertEquals(items, request.getItems());
    }
}