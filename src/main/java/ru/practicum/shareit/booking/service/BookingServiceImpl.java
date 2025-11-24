package ru.practicum.shareit.booking.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponseDto addBooking(Long bookerId, BookingRequestDto bookingRequestDto) {
        log.info("addBooking({}, {})", bookerId, bookingRequestDto);

        User booker = getUserOrThrow(bookerId);
        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item", bookingRequestDto.getItemId()));

        if (item.getOwner().getId().equals(bookerId)) {
            throw new ValidationException("Owner cannot be the same as booker");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available for booking");
        }

        Booking booking = Booking.builder()
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .item(item)
                .booker(booker)
                .status(Status.WAITING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        log.info("booking saved: {}", savedBooking);
        return bookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto updateStatus(Long userId, Long bookingId, Boolean approved) throws AccessDeniedException {
        log.info("updateStatus({}, {}, {})", userId, bookingId, approved);

        Booking booking = bookingRepository.findByIdWithItemAndOwner(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking", bookingId));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied!");
        }

        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("The reservation has already been processed. Current status: " + booking.getStatus());
        }

        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }

        Booking savedBooking = bookingRepository.save(booking);

        log.info("booking updated: {}", savedBooking);
        return bookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    public BookingResponseDto getById(Long bookingId, Long userId) throws AccessDeniedException {
        log.info("getById({}, {})", bookingId, userId);

        getUserOrThrow(userId);
        Booking booking = bookingRepository.findByIdWithItemAndBooker(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking", bookingId));

        if (booking.getBooker().getId().equals(userId) ||
            booking.getItem().getOwner().getId().equals(userId)) {
            log.info("booking getById: {}", bookingId);
            return bookingMapper.toBookingResponseDto(booking);
        } else {
            throw new AccessDeniedException("Access denied!");
        }
    }

    @Override
    public List<BookingResponseDto> getBookingsByBooker(Long bookerId, State state) {
        log.info("getBookingsByBooker({}, {})", bookerId, state);

        getUserOrThrow(bookerId);

        List<Booking> bookings = getBookingByStateForBooker(bookerId, state);

        bookings.sort(Comparator.comparing(Booking::getStart).reversed());

        log.info("bookings size: {}", bookings.size());
        return bookings.stream()
                .map(bookingMapper::toBookingResponseDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByOwner(Long ownerId, State state) {
        log.info("getBookingsByOwner({}, {})", ownerId, state);

        getUserOrThrow(ownerId);

        List<Booking> bookings = getBookingByStateForOwner(ownerId, state);

        bookings.sort(Comparator.comparing(Booking::getStart).reversed());

        log.info("bookings size: {}", bookings.size());
        return bookings.stream()
                .map(bookingMapper::toBookingResponseDto)
                .toList();
    }

    private List<Booking> getBookingByStateForBooker(Long bookerId, State state) {

        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                return bookingRepository.findAllByBookerId(bookerId);
            case CURRENT:
                return bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(bookerId, now, now);
            case PAST:
                return bookingRepository.findAllByBookerIdAndEndBefore(bookerId, now);
            case FUTURE:
                return bookingRepository.findAllByBookerIdAndStartAfter(bookerId, now);
            case WAITING:
                return bookingRepository.findAllByBookerIdAndStatus(bookerId, Status.WAITING);
            case REJECTED:
                return bookingRepository.findAllByBookerIdAndStatus(bookerId, Status.REJECTED);
            default:
                throw new ValidationException("Invalid state!");
        }
    }

    private List<Booking> getBookingByStateForOwner(Long ownerId, State state) {

        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                return bookingRepository.findAllByOwnerId(ownerId);
            case CURRENT:
                return bookingRepository.findAllByOwnerIdAndStartBeforeAndEndAfter(ownerId, now);
            case PAST:
                return bookingRepository.findAllByOwnerIdAndEndBefore(ownerId, now);
            case FUTURE:
                return bookingRepository.findAllByOwnerIdAndStartAfter(ownerId, now);
            case WAITING:
                return bookingRepository.findAllByOwnerIdAndStatus(ownerId, Status.WAITING);
            case REJECTED:
                return bookingRepository.findAllByOwnerIdAndStatus(ownerId, Status.REJECTED);
            default:
                throw new ValidationException("Invalid state!");
        }
    }

    private User getUserOrThrow(Long bookerId) {
        return userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("User", bookerId));
    }
}
