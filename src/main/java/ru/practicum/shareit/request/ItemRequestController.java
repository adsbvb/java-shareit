package ru.practicum.shareit.request;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestResponseDto createRequest(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @RequestBody ItemRequestDto itemRequestDto
    ) {
        log.debug("Request createRequest");
        return itemRequestService.createRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestResponseDto> getUserRequests(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId
    ) {
        log.debug("Request getUserRequests");
        return itemRequestService.getUserRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId
    ) {
        log.debug("Request getAllRequests");
        return itemRequestService.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getRequestById(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "User id must be a positive number") Long userId,
            @PathVariable(name = "requestId") Long requestId
    ) {
        log.debug("Request getRequestById");
        return itemRequestService.getRequestById(userId, requestId);
    }
}
