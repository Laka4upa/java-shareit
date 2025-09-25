package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingShortDto;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemDto createItem(ItemCreateDto itemCreateDto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден с Id " + ownerId));

        Item item = Item.builder()
                .name(itemCreateDto.getName())
                .description(itemCreateDto.getDescription())
                .available(itemCreateDto.getAvailable())
                .owner(owner)
                .requestId(itemCreateDto.getRequestId())
                .build();
        Item savedItem = itemRepository.save(item);
        return itemMapper.toDto(savedItem);
    }

    @Override
    public ItemDto getItemById(Long id, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Предмет не найден с id: " + id));

        ItemDto dto = itemMapper.toDto(item);
        dto.setOwnerId(item.getOwner().getId());

        List<CommentResponseDto> comments = getCommentsForItem(item.getId());
        dto.setComments(comments);

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> lastBookings = bookingRepository.findLastBookingForItem(item.getId(), now, PageRequest.of(0, 1));
            if (!lastBookings.isEmpty()) {
                dto.setLastBooking(createBookingShortDto(lastBookings.get(0)));
            }

            List<Booking> nextBookings = bookingRepository.findNextBookingForItem(item.getId(), now, PageRequest.of(0, 1));
            if (!nextBookings.isEmpty()) {
                dto.setNextBooking(createBookingShortDto(nextBookings.get(0)));
            }
        }

        return dto;
    }


    @Override
    public List<ItemDto> getAllItemsByOwnerId(Long ownerId) {
        List<Item> items = itemRepository.findByOwnerIdOrderById(ownerId);
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        List<Comment> comments = commentRepository.findByItemIdInOrderByCreatedDesc(itemIds);

        return items.stream()
                .map(item -> enrichItemWithBookingsAndComments(item, comments))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long id, ItemUpdateDto itemUpdateDto, Long ownerId) {
        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Предмет не найден с id: " + id));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new SecurityException("Только владелец может обновлять предмет");
        }

        if (itemUpdateDto.getName() != null) {
            existingItem.setName(itemUpdateDto.getName());
        }
        if (itemUpdateDto.getDescription() != null) {
            existingItem.setDescription(itemUpdateDto.getDescription());
        }
        if (itemUpdateDto.getAvailable() != null) {
            existingItem.setAvailable(itemUpdateDto.getAvailable());
        }
        if (itemUpdateDto.getRequestId() != null) {
            existingItem.setRequestId(itemUpdateDto.getRequestId());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return itemMapper.toDto(updatedItem);
    }

    @Override
    public List<ItemDto> searchAvailableItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchAvailableByText(text).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    private ItemDto enrichItemWithBookings(Item item) {
        ItemDto itemDto = itemMapper.toDto(item);

        LocalDateTime now = LocalDateTime.now();

        // Получаем последнее бронирование
        List<Booking> lastBookings = bookingRepository.findLastBookingForItem(
                item.getId(), now, PageRequest.of(0, 1));
        if (!lastBookings.isEmpty()) {
            itemDto.setLastBooking(createBookingShortDto(lastBookings.get(0)));
        }

        // Получаем следующее бронирование
        List<Booking> nextBookings = bookingRepository.findNextBookingForItem(
                item.getId(), now, PageRequest.of(0, 1));
        if (!nextBookings.isEmpty()) {
            itemDto.setNextBooking(createBookingShortDto(nextBookings.get(0)));
        }

        return itemDto;
    }

    private ItemDto enrichItemWithBookingsAndComments(Item item) {
        ItemDto itemDto = enrichItemWithBookings(item);
        List<CommentResponseDto> comments = getCommentsForItem(item.getId());
        itemDto.setComments(comments);
        return itemDto;
    }

    private ItemDto enrichItemWithBookingsAndComments(Item item, List<Comment> allComments) {
        ItemDto itemDto = enrichItemWithBookings(item);

        // Фильтруем комментарии для текущего предмета
        List<CommentResponseDto> comments = allComments.stream()
                .filter(comment -> comment.getItem().getId().equals(item.getId()))
                .map(commentMapper::toDto)
                .collect(Collectors.toList());

        itemDto.setComments(comments);
        return itemDto;
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long itemId, CommentRequestDto commentRequestDto, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден с id: " + userId));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Предмет не найден с id: " + itemId));

        // Проверяем, что пользователь действительно брал предмет в аренду
        boolean hasBooked = bookingRepository.existsByItemIdAndBookerIdAndEndTimeBefore(
                itemId, userId, LocalDateTime.now());

        if (!hasBooked) {
            throw new ValidationException("Пользователь не брал данный предмет в аренду");
        }

        Comment comment = commentMapper.toEntity(commentRequestDto);
        comment.setItem(item);
        comment.setAuthor(author);

        Comment savedComment = commentRepository.save(comment);
        log.info("Добавлен комментарий к предмету {} пользователем {}", itemId, userId);

        return commentMapper.toDto(savedComment);
    }

    private List<CommentResponseDto> getCommentsForItem(Long itemId) {
        return commentRepository.findByItemIdOrderByCreatedDesc(itemId).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    private BookingShortDto createBookingShortDto(Booking booking) {
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStartTime())
                .end(booking.getEndTime())
                .build();
    }
}