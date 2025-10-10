package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;

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

    @Test
    void createItem_WithValidData_ShouldReturnItemDto() {
        Long ownerId = 1L;
        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        User owner = User.builder().id(ownerId).build();
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

        User owner = User.builder().id(ownerId).build();
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
        Long ownerId = 1L;
        Long anotherUserId = 2L;
        ItemUpdateDto updateDto = new ItemUpdateDto();

        User owner = User.builder().id(ownerId).build();
        Item item = Item.builder().id(itemId).owner(owner).build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class,
                () -> itemService.updateItem(itemId, updateDto, anotherUserId));
    }

    @Test
    void updateItem_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        Long itemId = 1L;
        Long ownerId = 1L;
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Updated Name")
                .description(null)
                .available(null)
                .build();

        User owner = User.builder().id(ownerId).build();
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

        ItemDto result = itemService.updateItem(itemId, updateDto, ownerId);

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
        Long userId = 1L;
        CommentRequestDto commentRequest = CommentRequestDto.builder()
                .text("Comment")
                .build();

        User user = User.builder().id(userId).build();
        Item item = Item.builder().id(itemId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndEndTimeBefore(anyLong(), anyLong(), any()))
                .thenReturn(false);

        assertThrows(ValidationException.class,
                () -> itemService.addComment(itemId, commentRequest, userId));
    }

    @Test
    void addComment_WithValidBooking_ShouldCreateComment() {
        Long itemId = 1L;
        Long userId = 1L;
        CommentRequestDto commentRequest = CommentRequestDto.builder()
                .text("Great item!")
                .build();

        User user = User.builder().id(userId).build();
        Item item = Item.builder().id(itemId).build();
        Comment comment = Comment.builder().id(1L).build();
        CommentResponseDto expectedResponse = CommentResponseDto.builder().id(1L).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndEndTimeBefore(anyLong(), anyLong(), any()))
                .thenReturn(true);
        when(commentMapper.toEntity(commentRequest)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toDto(comment)).thenReturn(expectedResponse);

        CommentResponseDto result = itemService.addComment(itemId, commentRequest, userId);

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
}