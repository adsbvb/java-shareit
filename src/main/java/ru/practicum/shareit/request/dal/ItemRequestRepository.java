package ru.practicum.shareit.request.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    @Query("SELECT DISTINCT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.items i " +
            "LEFT JOIN FETCH i.owner " +
            "WHERE ir.requester.id = :requestedId " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findByRequesterIdOrderByCreateDesc(@Param("requestedId") long requestedId);

    @Query("SELECT DISTINCT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.items i " +
            "LEFT JOIN FETCH i.owner " +
            "WHERE ir.requester.id != :userId " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findAllByOtherId(@Param("userId") long requesterId);

    @Query("SELECT DISTINCT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.items i " +
            "LEFT JOIN FETCH i.owner " +
            "WHERE ir.id = :id")
    ItemRequest findById(@Param("id") long id);
}
