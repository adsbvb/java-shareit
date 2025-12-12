package ru.practicum.shareit.booking.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH i.owner WHERE b.id = :bookingId")
    Optional<Booking> findByIdWithItemAndOwner(
            @Param("bookingId") Long id
    );

    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH b.booker WHERE b.id = :bookingId")
    Optional<Booking> findByIdWithItemAndBooker(
            @Param("bookingId") Long id
    );

    List<Booking> findAllByBookerId(
            Long id
    );

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfter(
            Long id, LocalDateTime start, LocalDateTime end
    );

    List<Booking> findAllByBookerIdAndEndBefore(
            Long id, LocalDateTime end
    );

    List<Booking> findAllByBookerIdAndStartAfter(
            Long id, LocalDateTime start
    );

    List<Booking> findAllByBookerIdAndStatus(
            Long id, Status  status
    );

    @Query("SELECT b FROM Booking b JOIN b.item i WHERE i.owner.id = :ownerId")
    List<Booking> findAllByOwnerId(
            @Param("ownerId") Long ownerId
    );

    @Query("SELECT b FROM Booking b JOIN b.item i WHERE i.owner.id = :ownerId AND b.start <= :now AND b.end >= :now")
    List<Booking> findAllByOwnerIdAndStartBeforeAndEndAfter(
            @Param("ownerId") Long id,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT b FROM Booking b JOIN b.item i WHERE i.owner.id = :ownerId AND b.end < :end")
    List<Booking> findAllByOwnerIdAndEndBefore(
            @Param("ownerId") Long id,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT b FROM Booking b JOIN b.item i WHERE i.owner.id = :ownerId AND b.start > :start")
    List<Booking> findAllByOwnerIdAndStartAfter(
            @Param("ownerId") Long id,
            @Param("start") LocalDateTime start
    );

    @Query("SELECT b FROM Booking b JOIN b.item i WHERE i.owner.id = :ownerId AND b.status = :status")
    List<Booking> findAllByOwnerIdAndStatus(
            @Param("ownerId") Long id,
            @Param("status") Status status
    );

    List<Booking> findByBookerIdAndItemIdAndEndBeforeAndStatus(
            Long bookerId, Long itemId, LocalDateTime end, Status status
    );

    List<Booking> findByItemIdAndEndBeforeOrderByEndDesc(
            Long itemId,
            LocalDateTime end
    );

    List<Booking> findByItemIdAndStartAfterOrderByStartAsc(
            Long itemId, LocalDateTime now
    );

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN :itemIds " +
            "AND b.end < :now " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.item.id, b.end DESC")
    List<Booking> findLastBookingsForItems(
            @Param("itemIds") List<Long> itemIds,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN :itemIds " +
            "AND b.start > :now " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.item.id, b.start ASC")
    List<Booking> findNextBookingsForItems(
            @Param("itemIds") List<Long> itemIds,
            @Param("now") LocalDateTime now
    );

    default Map<Long, List<Booking>> findLastBookingsForItems(List<Long> itemIds) {
        List<Booking> bookings = findLastBookingsForItems(itemIds, LocalDateTime.now());
        return bookings.stream().collect(Collectors.groupingBy(b -> b.getItem().getId()));
    }

    default Map<Long, List<Booking>> findNextBookingsForItems(List<Long> itemIds) {
        List<Booking> bookings = findNextBookingsForItems(itemIds, LocalDateTime.now());
        return bookings.stream().collect(Collectors.groupingBy(b -> b.getItem().getId()));
    }
}
