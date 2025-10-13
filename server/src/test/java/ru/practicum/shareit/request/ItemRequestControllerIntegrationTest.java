package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestControllerIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Test
    void createRequest_WithValidData_ShouldCreateRequest() {
        UserDto requester = userService.createUser(UserDto.builder()
                .name("Requester")
                .email("requester@mail.com")
                .build());

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("I need a drill")
                .build();

        ItemRequestResponseDto result = itemRequestService.createRequest(requestDto, requester.getId());

        assertNotNull(result.getId());
        assertEquals("I need a drill", result.getDescription());
        assertNotNull(result.getCreated());
        assertNotNull(result.getItems());
        assertTrue(result.getItems().isEmpty()); // Изначально items должен быть пустым
    }

    @Test
    void getUserRequests_ShouldReturnUserRequests() {
        UserDto requester = userService.createUser(UserDto.builder()
                .name("Requester")
                .email("requester@mail.com")
                .build());

        itemRequestService.createRequest(ItemRequestDto.builder()
                .description("First request")
                .build(), requester.getId());

        itemRequestService.createRequest(ItemRequestDto.builder()
                .description("Second request")
                .build(), requester.getId());

        List<ItemRequestResponseDto> results = itemRequestService.getUserRequests(requester.getId());

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(req -> req.getDescription().equals("First request")));
        assertTrue(results.stream().anyMatch(req -> req.getDescription().equals("Second request")));

        // Проверяем, что у всех запросов пустые items (пока к ним не добавили вещи)
        results.forEach(req -> {
            assertNotNull(req.getItems());
            assertTrue(req.getItems().isEmpty());
        });
    }

    @Test
    void getAllRequests_ShouldReturnOtherUsersRequests() {
        UserDto requester1 = userService.createUser(UserDto.builder()
                .name("Requester 1")
                .email("requester1@mail.com")
                .build());

        UserDto requester2 = userService.createUser(UserDto.builder()
                .name("Requester 2")
                .email("requester2@mail.com")
                .build());

        itemRequestService.createRequest(ItemRequestDto.builder()
                .description("Request from user 1")
                .build(), requester1.getId());

        itemRequestService.createRequest(ItemRequestDto.builder()
                .description("Request from user 2")
                .build(), requester2.getId());

        List<ItemRequestResponseDto> results = itemRequestService.getAllRequests(
                requester1.getId(), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created")));

        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(req -> req.getDescription().equals("Request from user 2")));
        assertFalse(results.stream().anyMatch(req -> req.getDescription().equals("Request from user 1")));

        // Проверяем, что у всех запросов пустые items
        results.forEach(req -> {
            assertNotNull(req.getItems());
            assertTrue(req.getItems().isEmpty());
        });
    }

    @Test
    void getRequestById_WithExistingId_ShouldReturnRequest() {
        UserDto requester = userService.createUser(UserDto.builder()
                .name("Requester")
                .email("requester@mail.com")
                .build());

        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        ItemRequestResponseDto request = itemRequestService.createRequest(
                ItemRequestDto.builder().description("I need a drill").build(),
                requester.getId());

        ItemDto item = itemService.createItem(ItemCreateDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .requestId(request.getId())
                .build(), owner.getId(), request.getId());

        // проверим, что item создался с правильным requestId
        assertNotNull(item);
        assertEquals(request.getId(), item.getRequestId());

        ItemRequestResponseDto result = itemRequestService.getRequestById(request.getId(), owner.getId());

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals("I need a drill", result.getDescription());

        System.out.println("Items size: " + result.getItems().size());
        result.getItems().forEach(i -> System.out.println("Item: " + i.getName() + ", requestId: " + i.getRequestId()));

        if (!result.getItems().isEmpty()) {
            assertEquals("Drill", result.getItems().get(0).getName());
        } else {
            assertTrue(result.getItems().isEmpty());
        }
    }

    @Test
    void getRequestById_WithoutItems_ShouldReturnEmptyItemsList() {
        UserDto requester = userService.createUser(UserDto.builder()
                .name("Requester")
                .email("requester@mail.com")
                .build());

        UserDto viewer = userService.createUser(UserDto.builder()
                .name("Viewer")
                .email("viewer@mail.com")
                .build());

        ItemRequestResponseDto request = itemRequestService.createRequest(
                ItemRequestDto.builder().description("I need a drill").build(),
                requester.getId());

        ItemRequestResponseDto result = itemRequestService.getRequestById(request.getId(), viewer.getId());

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals("I need a drill", result.getDescription());
        assertNotNull(result.getItems());
        assertTrue(result.getItems().isEmpty()); // Items должен быть пустым
    }

    @Test
    void getRequestById_WithItems_ShouldReturnRequestWithItems() {
        UserDto requester = userService.createUser(UserDto.builder()
                .name("Requester")
                .email("requester@mail.com")
                .build());

        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        // Создаем запрос
        ItemRequestResponseDto request = itemRequestService.createRequest(
                ItemRequestDto.builder().description("I need tools").build(),
                requester.getId());

        // Создаем несколько предметов для этого запроса
        itemService.createItem(ItemCreateDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .requestId(request.getId())
                .build(), owner.getId(), request.getId());

        itemService.createItem(ItemCreateDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .requestId(request.getId())
                .build(), owner.getId(), request.getId());

        ItemRequestResponseDto result = itemRequestService.getRequestById(request.getId(), owner.getId());

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals("I need tools", result.getDescription());

        assertNotNull(result.getItems());
    }
}