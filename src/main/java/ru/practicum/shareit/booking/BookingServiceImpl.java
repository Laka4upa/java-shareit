package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
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
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;
    private ForbiddenException forbiddenException;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long bookerId) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден с id: " + bookerId));

        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Предмет не найден с id: " + bookingRequestDto.getItemId()));

        // Проверка доступности предмета
        if (!item.getAvailable()) {
            throw new ValidationException("Предмет недоступен для бронирования");
        }

        // Проверка, что владелец не бронирует свой предмет
        if (item.getOwner().getId().equals(bookerId)) {
            throw new ConflictException("Владелец не может бронировать свой предмет");
        }

        // Проверка дат
        if (bookingRequestDto.getStart().isAfter(bookingRequestDto.getEnd())) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }

        if (bookingRequestDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала должна быть в будущем");
        }

        boolean hasOverlap = bookingRepository.existsOverlappingBookings(
                item.getId(),
                bookingRequestDto.getStart(),
                bookingRequestDto.getEnd()
        );

        if (hasOverlap) {
            throw new ValidationException("Предмет уже забронирован на указанные даты");
        }

        Booking booking = bookingMapper.toEntity(bookingRequestDto);
        booking.setItem(item);
        booking.setBooker(booker);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Создано бронирование с id: {}", savedBooking.getId());

        return bookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long bookingId, Boolean approved, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование не найдено с id: " + bookingId));

        // Проверка прав владельца
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw forbiddenException;
        }

        // Проверка статуса
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ConflictException("Бронирование уже обработано");
        }

        if (approved) {
            boolean hasOverlap = bookingRepository.existsOverlappingBookingsExcluding(
                    booking.getItem().getId(),
                    booking.getStartTime(),
                    booking.getEndTime(),
                    bookingId
            );

            if (hasOverlap) {
                throw new ValidationException("Нельзя подтвердить бронирование - есть пересечение с другими бронированиями");
            }
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Бронирование {} {}", bookingId, approved ? "подтверждено" : "отклонено");

        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование не найдено с id: " + bookingId));

        // Проверка прав доступа
        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NoSuchElementException("Доступ к бронированию запрещен");
        }

        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, BookingState state, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден с id: " + userId));

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerIdOrderByStartTimeDesc(userId, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(
                        userId, now, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndTimeBeforeOrderByStartTimeDesc(userId, now, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartTimeAfterOrderByStartTimeDesc(userId, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartTimeDesc(userId,
                        BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartTimeDesc(userId,
                        BookingStatus.REJECTED, pageable);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, BookingState state, Pageable pageable) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден с id: " + ownerId));

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartTimeDesc(ownerId, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(
                        ownerId, now, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository
                        .findByItemOwnerIdAndEndTimeBeforeOrderByStartTimeDesc(ownerId, now, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository
                        .findByItemOwnerIdAndStartTimeAfterOrderByStartTimeDesc(ownerId, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartTimeDesc(ownerId,
                        BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartTimeDesc(ownerId,
                        BookingStatus.REJECTED, pageable);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }
}