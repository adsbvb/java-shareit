package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.request.dal.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    @Transactional
    public ItemRequestResponseDto createRequest(Long userId, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User",  userId));

        ItemRequest request = itemRequestMapper.toItemRequest(itemRequestDto);

        request.setRequester(user);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(request);
        log.info("Created new item request with id: {}", savedRequest.getId());

        return itemRequestMapper.toResponseDto(savedRequest);
    }

    @Override
    public List<ItemRequestResponseDto> getUserRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User",  userId);
        }

        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdOrderByCreateDesc(userId);
        log.info("Found {} requests for user with id: {}", requests.size(), userId);

        return requests.stream()
                .map(itemRequestMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User",  userId);
        }

        List<ItemRequest> requests = itemRequestRepository.findAllByOtherId(userId);
        log.info("Found {} requests from other users for user with ID: {}", requests.size(), userId);

        return requests.stream()
                .map(itemRequestMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId, Integer from, Integer size) {
        if (from == null || size == null) {
            return getAllRequests(userId);
        }

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());
        List<ItemRequest> requests = itemRequestRepository.findAllByOtherId(userId, pageable);

        log.info("Found {} requests from other users for user with ID: {}, from={}, size={}",
                requests.size(), userId, from, size);

        return requests.stream()
                .map(itemRequestMapper::toResponseDto)
                .toList();
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User",  userId);
        }

        ItemRequest foundRequest = itemRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new NotFoundException("Request",  requestId));

        log.info("Retrieved request with id: {} for user with id: {}", requestId, userId);

        return itemRequestMapper.toResponseDto(foundRequest);
    }
}
