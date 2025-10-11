package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemControllerIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void createItem_WithValidData_ShouldCreateItem() {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        ItemDto result = itemService.createItem(itemCreateDto, owner.getId(), null);

        assertNotNull(result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void getItemById_ShouldReturnItemWithDetails() {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        ItemDto item = itemService.createItem(ItemCreateDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build(), owner.getId(), null);

        ItemDto result = itemService.getItemById(item.getId(), owner.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals("Item", result.getName());
        assertNotNull(result.getComments());
    }

    @Test
    void getAllItemsByOwner_ShouldReturnOwnerItems() {
        // Given
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        itemService.createItem(ItemCreateDto.builder()
                .name("Item 1")
                .description("Description 1")
                .available(true)
                .build(), owner.getId(), null);

        itemService.createItem(ItemCreateDto.builder()
                .name("Item 2")
                .description("Description 2")
                .available(true)
                .build(), owner.getId(), null);

        List<ItemDto> results = itemService.getAllItemsByOwnerId(owner.getId());

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(item -> item.getName().equals("Item 1")));
        assertTrue(results.stream().anyMatch(item -> item.getName().equals("Item 2")));
    }

    @Test
    void searchAvailableItems_ShouldReturnOnlyAvailableItems() {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        itemService.createItem(ItemCreateDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build(), owner.getId(), null);

        itemService.createItem(ItemCreateDto.builder()
                .name("Broken Drill")
                .description("Not working")
                .available(false)
                .build(), owner.getId(), null);

        List<ItemDto> results = itemService.searchAvailableItems("drill");

        assertEquals(1, results.size());
        assertEquals("Drill", results.get(0).getName());
        assertTrue(results.get(0).getAvailable());
    }

    @Test
    void addComment_WithoutBooking_ShouldThrowException() {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        UserDto randomUser = userService.createUser(UserDto.builder()
                .name("Random User")
                .email("random@mail.com")
                .build());

        ItemDto item = itemService.createItem(ItemCreateDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build(), owner.getId(), null);

        CommentRequestDto commentRequest = CommentRequestDto.builder()
                .text("Great item!")
                .build();

        assertThrows(Exception.class,
                () -> itemService.addComment(item.getId(), commentRequest, randomUser.getId()));
    }
}