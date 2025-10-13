package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.dto.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Бронирования пользователя с фильтром по статусу
    List<Booking> findByBookerIdOrderByStartTimeDesc(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartTimeDesc(Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findByBookerIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(
            Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndEndTimeBeforeOrderByStartTimeDesc(
            Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartTimeAfterOrderByStartTimeDesc(
            Long bookerId, LocalDateTime start, Pageable pageable);

    // Бронирования владельца с фильтром по статусу
    List<Booking> findByItemOwnerIdOrderByStartTimeDesc(Long ownerId, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartTimeDesc(
            Long ownerId, BookingStatus status, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(
            Long ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndEndTimeBeforeOrderByStartTimeDesc(
            Long ownerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartTimeAfterOrderByStartTimeDesc(
            Long ownerId, LocalDateTime start, Pageable pageable);

    // Последнее и следующее бронирование для предмета
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' " +
            "AND b.endTime < :currentTime ORDER BY b.endTime DESC")
    List<Booking> findLastBookingForItem(@Param("itemId") Long itemId, @Param("currentTime") LocalDateTime currentTime,
                                         Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' AND " +
            "b.startTime > :currentTime ORDER BY b.startTime ASC")
    List<Booking> findNextBookingForItem(@Param("itemId") Long itemId, @Param("currentTime") LocalDateTime currentTime,
                                         Pageable pageable);

    // Проверка существования бронирований для предмета пользователем
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.item.id = :itemId AND b.booker.id = :userId " +
            "AND b.status = 'APPROVED' AND b.endTime < :currentTime")
    boolean existsByItemIdAndBookerIdAndEndTimeBefore(@Param("itemId") Long itemId,
                                                      @Param("userId") Long userId,
                                                      @Param("currentTime") LocalDateTime currentTime);

    // Проверяет, есть ли пересекающиеся APPROVED/WAITING бронирования для предмета

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status IN ('APPROVED', 'WAITING') " +
            "AND (:startTime < b.endTime AND :endTime > b.startTime)")
    boolean existsOverlappingBookings(@Param("itemId") Long itemId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    // Проверяет пересечения, исключая текущее бронирование (для обновлений)
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.id <> :excludedBookingId " +
            "AND b.status IN ('APPROVED', 'WAITING') " +
            "AND (:startTime < b.endTime AND :endTime > b.startTime)")
    boolean existsOverlappingBookingsExcluding(@Param("itemId") Long itemId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("excludedBookingId") Long excludedBookingId);
}