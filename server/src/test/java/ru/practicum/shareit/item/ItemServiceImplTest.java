package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private User otherUser;
    private Item item;
    private ItemDto itemDto;
    private Booking lastBooking;
    private Booking nextBooking;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "owner", "owner@mail.ru");
        booker = new User(2L, "booker", "booker@mail.ru");
        otherUser = new User(3L, "other", "other@mail.ru");
        item = new Item(1L, "item", "desc", true, owner, null);
        now = LocalDateTime.now();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("item")
                .description("desc")
                .available(true)
                .ownerId(1L)
                .comments(List.of())
                .build();

        lastBooking = new Booking(1L, now.minusHours(2), now.minusHours(1), item, booker, BookingStatus.APPROVED);
        nextBooking = new Booking(2L, now.plusHours(1), now.plusHours(2), item, booker, BookingStatus.APPROVED);
    }

    @Test
    void createItem_WithValidData_ShouldReturnItemDto() {
        Long ownerId = 1L;
        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        Item item = Item.builder().id(1L).build();
        ItemDto expectedDto = ItemDto.builder().id(1L).build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(expectedDto);

        ItemDto result = itemService.createItem(createDto, ownerId, null);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_WithRequest_ShouldCreateItemWithRequest() {
        Long ownerId = 1L;
        Long requestId = 10L;
        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .requestId(requestId)
                .build();

        ItemRequest request = ItemRequest.builder().id(requestId).build();
        Item item = Item.builder().id(1L).build();
        ItemDto expectedDto = ItemDto.builder().id(1L).build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(expectedDto);

        ItemDto result = itemService.createItem(createDto, ownerId, requestId);

        assertNotNull(result);
        verify(itemRequestRepository).findById(requestId);
    }

    @Test
    void updateItem_WhenUserNotOwner_ShouldThrowForbiddenException() {
        Long itemId = 1L;
        ItemUpdateDto updateDto = new ItemUpdateDto();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class,
                () -> itemService.updateItem(itemId, updateDto, otherUser.getId()));
    }

    @Test
    void updateItem_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        Long itemId = 1L;
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Updated Name")
                .description(null)
                .available(null)
                .build();

        Item existingItem = Item.builder()
                .id(itemId)
                .name("Original Name")
                .description("Original Description")
                .available(true)
                .owner(owner)
                .build();

        Item updatedItem = Item.builder()
                .id(itemId)
                .name("Updated Name")
                .description("Original Description")
                .available(true)
                .owner(owner)
                .build();

        ItemDto expectedDto = ItemDto.builder().id(itemId).build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.toDto(updatedItem)).thenReturn(expectedDto);

        ItemDto result = itemService.updateItem(itemId, updateDto, owner.getId());

        assertNotNull(result);
        verify(itemRepository).save(argThat(item ->
                item.getName().equals("Updated Name") &&
                        item.getDescription().equals("Original Description") &&
                        item.getAvailable()
        ));
    }

    @Test
    void addComment_WhenUserDidNotBookItem_ShouldThrowValidationException() {
        Long itemId = 1L;
        CommentRequestDto commentRequest = CommentRequestDto.builder()
                .text("Comment")
                .build();

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndEndTimeBefore(anyLong(), anyLong(), any()))
                .thenReturn(false);

        assertThrows(ValidationException.class,
                () -> itemService.addComment(itemId, commentRequest, booker.getId()));
    }

    @Test
    void addComment_WithValidBooking_ShouldCreateComment() {
        Long itemId = 1L;
        CommentRequestDto commentRequest = CommentRequestDto.builder()
                .text("Great item!")
                .build();

        Comment comment = Comment.builder().id(1L).build();
        CommentResponseDto expectedResponse = CommentResponseDto.builder().id(1L).build();

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndEndTimeBefore(anyLong(), anyLong(), any()))
                .thenReturn(true);
        when(commentMapper.toEntity(commentRequest)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toDto(comment)).thenReturn(expectedResponse);

        CommentResponseDto result = itemService.addComment(itemId, commentRequest, booker.getId());

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void searchAvailableItems_WithBlankText_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchAvailableItems("   ");

        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchAvailableByText(anyString());
    }

    @Test
    void searchAvailableItems_WithText_ShouldReturnItems() {
        String searchText = "drill";
        Item item = Item.builder().id(1L).build();
        ItemDto itemDto = ItemDto.builder().id(1L).build();

        when(itemRepository.searchAvailableByText(searchText)).thenReturn(List.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.searchAvailableItems(searchText);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(itemRepository).searchAvailableByText(searchText.toLowerCase());
    }

    @Test
    void getItemById_WhenUserNotOwner_ShouldNotIncludeBookings() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(List.of());

        ItemDto result = itemService.getItemById(1L, otherUser.getId());

        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
        assertThat(result.getComments()).isEmpty();
    }

    @Test
    void getItemById_WhenNoLastBooking_ShouldReturnNullLastBooking() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);
        when(bookingRepository.findLastBookingForItem(eq(1L), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(List.of());
        when(bookingRepository.findNextBookingForItem(eq(1L), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(List.of(nextBooking));
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(List.of());

        ItemDto result = itemService.getItemById(1L, owner.getId());

        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNotNull();
    }

    @Test
    void getItemById_WhenNoNextBooking_ShouldReturnNullNextBooking() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);
        when(bookingRepository.findLastBookingForItem(eq(1L), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(List.of(lastBooking));
        when(bookingRepository.findNextBookingForItem(eq(1L), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(List.of());
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(List.of());

        ItemDto result = itemService.getItemById(1L, owner.getId());

        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void searchAvailableItems_WithNullText_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchAvailableItems(null);

        assertThat(result).isEmpty();
    }

    @Test
    void updateItemRequest_WithValidData_ShouldUpdateRequest() {
        ItemRequest request = new ItemRequest(1L, "Need item", owner, LocalDateTime.now(), List.of());

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.updateItemRequest(1L, 1L, owner.getId());

        assertThat(result).isNotNull();
    }

    @Test
    void updateItemRequest_WithNullRequestId_ShouldSetRequestToNull() {
        Item itemWithRequest = new Item(1L, "item", "desc", true, owner, new ItemRequest());

        when(itemRepository.findById(1L)).thenReturn(Optional.of(itemWithRequest));
        when(itemRepository.save(any(Item.class))).thenReturn(itemWithRequest);
        when(itemMapper.toDto(itemWithRequest)).thenReturn(itemDto);

        ItemDto result = itemService.updateItemRequest(1L, null, owner.getId());

        assertThat(result).isNotNull();
    }

    @Test
    void updateItemRequest_WhenUserNotOwner_ShouldThrowForbiddenException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class, () ->
                itemService.updateItemRequest(1L, 1L, otherUser.getId()));
    }

    @Test
    void updateItemRequest_WhenRequestNotFound_ShouldThrowException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                itemService.updateItemRequest(1L, 1L, owner.getId()));
    }
}