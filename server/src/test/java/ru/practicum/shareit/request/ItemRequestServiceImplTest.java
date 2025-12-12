package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.request.dal.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemRequestServiceImpl unit tests")
class ItemRequestServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private ItemRequestDto itemRequestDto;
    private ItemRequest itemRequest;
    private ItemRequestResponseDto itemRequestResponseDto;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();

        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .description("Нужна дрель для ремонта дома")
                .build();

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель для ремонта дома")
                .requester(user)
                .created(testTime)
                .build();

        itemRequestResponseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Нужна дрель для ремонта дома")
                .created(testTime)
                .items(List.of())
                .build();
    }

    @Test
    void createRequest_ValidData_ShouldCreateRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestMapper.toItemRequest(itemRequestDto)).thenReturn(itemRequest);
        when(itemRequestRepository.save(itemRequest)).thenReturn(itemRequest);
        when(itemRequestMapper.toResponseDto(itemRequest)).thenReturn(itemRequestResponseDto);

        ItemRequestResponseDto result = itemRequestService.createRequest(1L, itemRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Нужна дрель для ремонта дома");
        assertThat(result.getCreated()).isEqualTo(testTime);

        verify(userRepository, times(1)).findById(1L);
        verify(itemRequestMapper, times(1)).toItemRequest(itemRequestDto);
        verify(itemRequestRepository, times(1)).save(itemRequest);
        verify(itemRequestMapper, times(1)).toResponseDto(itemRequest);

        assertThat(itemRequest.getRequester()).isEqualTo(user);
        assertThat(itemRequest.getCreated()).isNotNull();
    }

    @Test
    void createRequest_NonExistentUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.createRequest(999L, itemRequestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("999");

        verify(userRepository, times(1)).findById(999L);
        verify(itemRequestMapper, never()).toItemRequest(any());
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void getUserRequests_ShouldReturnUserRequests() {
        List<ItemRequest> requests = List.of(itemRequest);
        List<ItemRequestResponseDto> expectedDtos = List.of(itemRequestResponseDto);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRequestRepository.findByRequesterIdOrderByCreateDesc(1L)).thenReturn(requests);
        when(itemRequestMapper.toResponseDto(itemRequest)).thenReturn(itemRequestResponseDto);

        List<ItemRequestResponseDto> result = itemRequestService.getUserRequests(1L);

        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(expectedDtos);

        verify(userRepository, times(1)).existsById(1L);
        verify(itemRequestRepository, times(1)).findByRequesterIdOrderByCreateDesc(1L);
        verify(itemRequestMapper, times(1)).toResponseDto(itemRequest);
    }

    @Test
    void getUserRequests_NonExistentUser_ShouldThrowNotFoundException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> itemRequestService.getUserRequests(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("999");

        verify(userRepository, times(1)).existsById(999L);
        verify(itemRequestRepository, never()).findByRequesterIdOrderByCreateDesc(anyLong());
    }

    @Test
    void getUserRequests_NoRequests_ShouldReturnEmptyList() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRequestRepository.findByRequesterIdOrderByCreateDesc(1L)).thenReturn(List.of());

        List<ItemRequestResponseDto> result = itemRequestService.getUserRequests(1L);

        assertThat(result).isEmpty();

        verify(userRepository, times(1)).existsById(1L);
        verify(itemRequestRepository, times(1)).findByRequesterIdOrderByCreateDesc(1L);
        verify(itemRequestMapper, never()).toResponseDto(any());
    }

    @Test
    void getAllRequests_ShouldReturnOtherUsersRequests() {
        List<ItemRequest> requests = List.of(itemRequest);
        List<ItemRequestResponseDto> expectedDtos = List.of(itemRequestResponseDto);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRequestRepository.findAllByOtherId(1L)).thenReturn(requests);
        when(itemRequestMapper.toResponseDto(itemRequest)).thenReturn(itemRequestResponseDto);

        List<ItemRequestResponseDto> result = itemRequestService.getAllRequests(1L);

        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(expectedDtos);

        verify(userRepository, times(1)).existsById(1L);
        verify(itemRequestRepository, times(1)).findAllByOtherId(1L);
        verify(itemRequestMapper, times(1)).toResponseDto(itemRequest);
    }

    @Test
    void getAllRequests_WithPagination_ShouldReturnOtherUsersRequests() {
        int from = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(0, size, Sort.by("created").descending());

        List<ItemRequest> requests = List.of(itemRequest);
        List<ItemRequestResponseDto> expectedDtos = List.of(itemRequestResponseDto);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRequestRepository.findAllByOtherId(1L, pageable)).thenReturn(requests);
        when(itemRequestMapper.toResponseDto(itemRequest)).thenReturn(itemRequestResponseDto);

        List<ItemRequestResponseDto> result = itemRequestService.getAllRequests(1L, from, size);

        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(expectedDtos);

        verify(userRepository, times(1)).existsById(1L);
        verify(itemRequestRepository, times(1)).findAllByOtherId(1L, pageable);
        verify(itemRequestMapper, times(1)).toResponseDto(itemRequest);
    }

    @Test
    void getRequestById_ShouldReturnRequest() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRequestRepository.findByRequestId(1L)).thenReturn(Optional.of(itemRequest));
        when(itemRequestMapper.toResponseDto(itemRequest)).thenReturn(itemRequestResponseDto);

        ItemRequestResponseDto result = itemRequestService.getRequestById(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Нужна дрель для ремонта дома");

        verify(userRepository, times(1)).existsById(1L);
        verify(itemRequestRepository, times(1)).findByRequestId(1L);
        verify(itemRequestMapper, times(1)).toResponseDto(itemRequest);
    }

    @Test
    void getRequestById_NonExistentUser_ShouldThrowNotFoundException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> itemRequestService.getRequestById(999L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("999");

        verify(userRepository, times(1)).existsById(999L);
        verify(itemRequestRepository, never()).findByRequestId(anyLong());
    }

    @Test
    void getRequestById_NonExistentRequest_ShouldThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRequestRepository.findByRequestId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.getRequestById(1L, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Request")
                .hasMessageContaining("999");

        verify(userRepository, times(1)).existsById(1L);
        verify(itemRequestRepository, times(1)).findByRequestId(999L);
        verify(itemRequestMapper, never()).toResponseDto(any());
    }
}