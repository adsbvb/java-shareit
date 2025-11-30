package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestResponseDto createRequest(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestResponseDto> getUserRequests(Long userId);

    List<ItemRequestResponseDto> getAllRequests(Long userId);

    ItemRequestResponseDto getRequestById(Long userId, Long requestId);
}
