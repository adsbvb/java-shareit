package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.request.dal.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ItemRequestServiceImpl.createRequest() integration tests")
public class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestMapper itemRequestMapper;

    private User user;
    private ItemRequestDto requestDto;
    private LocalDateTime testStartTime;

    @BeforeEach
    void setUp() {
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();

        testStartTime = LocalDateTime.now();

        user = userRepository.save(User.builder()
                .name("Test User")
                .email("test@example.com")
                .build());

        requestDto = ItemRequestDto.builder()
                .description("Нужна дрель для ремонта дома")
                .build();
    }

    @Test
    void createRequest_ValidData_ShouldCreateRequestSuccessfully() {
        Long userId = user.getId();

        ItemRequestResponseDto result = itemRequestService.createRequest(userId, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Нужна дрель для ремонта дома");
        assertThat(result.getItems()).isEmpty();

        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getCreated()).isAfterOrEqualTo(testStartTime);
        assertThat(result.getCreated()).isBeforeOrEqualTo(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.findById(result.getId()).orElseThrow();
        assertThat(savedRequest.getDescription()).isEqualTo("Нужна дрель для ремонта дома");
        assertThat(savedRequest.getRequester().getId()).isEqualTo(userId);
        assertThat(savedRequest.getCreated()).isEqualTo(result.getCreated());
    }

    @Test
    void createRequest_NonExistentUser_ShouldThrowNotFoundException() {
        Long nonExistentUserId = 9999L;

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            itemRequestService.createRequest(nonExistentUserId, requestDto);
        });

        assertThat(exception.getMessage()).contains("User");
        assertThat(exception.getMessage()).contains(nonExistentUserId.toString());

        assertThat(itemRequestRepository.count()).isZero();
    }

    @Test
    void createRequest_MultipleRequestsForSameUser_ShouldWorkCorrectly() {
        ItemRequestDto requestDto1 = ItemRequestDto.builder()
                .description("Первый запрос")
                .build();

        ItemRequestDto requestDto2 = ItemRequestDto.builder()
                .description("Второй запрос")
                .build();

        ItemRequestDto requestDto3 = ItemRequestDto.builder()
                .description("Третий запрос")
                .build();

        ItemRequestResponseDto result1 = itemRequestService.createRequest(user.getId(), requestDto1);
        ItemRequestResponseDto result2 = itemRequestService.createRequest(user.getId(), requestDto2);
        ItemRequestResponseDto result3 = itemRequestService.createRequest(user.getId(), requestDto3);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result3).isNotNull();

        assertThat(result1.getId()).isNotEqualTo(result2.getId());
        assertThat(result2.getId()).isNotEqualTo(result3.getId());
        assertThat(result1.getId()).isNotEqualTo(result3.getId());

        assertThat(itemRequestRepository.count()).isEqualTo(3);
    }

    @Test
    void createRequest_ShouldSetRequesterCorrectly() {
        ItemRequestResponseDto result = itemRequestService.createRequest(user.getId(), requestDto);

        ItemRequest savedRequest = itemRequestRepository.findById(result.getId()).orElseThrow();

        assertThat(savedRequest.getRequester()).isNotNull();
        assertThat(savedRequest.getRequester().getId()).isEqualTo(user.getId());
        assertThat(savedRequest.getRequester().getName()).isEqualTo(user.getName());

        assertThat(savedRequest.getRequester().getId()).isEqualTo(user.getId());
    }
}
