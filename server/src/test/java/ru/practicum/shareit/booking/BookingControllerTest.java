package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@DisplayName("BookingController tests (service module)")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingRequestDto bookingRequestDto;
    private BookingResponseDto bookingResponseDto;
    private ItemResponseDto itemResponseDto;
    private UserDto userDto;
    private String bookingRequestJson;

    @BeforeEach
    void setUp() throws Exception {
        bookingRequestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        itemResponseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Электрическая дрель")
                .description("Test Description")
                .available(true)
                .requestId(1L)
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("Booker Name")
                .build();

        bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(ru.practicum.shareit.booking.model.Status.WAITING)
                .booker(userDto)
                .item(itemResponseDto)
                .build();

        bookingRequestJson = objectMapper.writeValueAsString(bookingRequestDto);
    }

    @Test
    void addBooking_ValidData_ReturnsCreatedBooking() throws Exception {
        when(bookingService.addBooking(anyLong(), any(BookingRequestDto.class)))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.item.id").value(1L))
                .andExpect(jsonPath("$.item.name").value("Электрическая дрель"))
                .andExpect(jsonPath("$.booker.id").value(1L))
                .andExpect(jsonPath("$.booker.name").value("Booker Name"));
    }

    @Test
    void updateStatus_ValidData_ReturnsUpdatedBooking() throws Exception {
        bookingResponseDto.setStatus(ru.practicum.shareit.booking.model.Status.APPROVED);

        when(bookingService.updateStatus(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void updateStatus_AccessDenied_ReturnsForbidden() throws Exception {
        when(bookingService.updateStatus(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new AccessDeniedException("Only item owner can approve booking"));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBookingById_ValidData_ReturnsBooking() throws Exception {
        when(bookingService.getById(anyLong(), anyLong()))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.item.id").value(1L))
                .andExpect(jsonPath("$.booker.id").value(1L));
    }

    @Test
    void getBookingById_AccessDenied_ReturnsForbidden() throws Exception {
        when(bookingService.getById(anyLong(), anyLong()))
                .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 999L))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBookingById_BookingNotFound_ReturnsNotFound() throws Exception {
        when(bookingService.getById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Booking", 999L));

        mockMvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingsByBooker_ValidData_ReturnsBookings() throws Exception {
        BookingResponseDto booking2 = BookingResponseDto.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .status(ru.practicum.shareit.booking.model.Status.APPROVED)
                .build();

        List<BookingResponseDto> bookings = List.of(bookingResponseDto, booking2);

        when(bookingService.getBookingsByBooker(anyLong(), any(State.class)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].status").value("APPROVED"));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingsByOwner_ValidData_ReturnsBookings() throws Exception {
        BookingResponseDto booking2 = BookingResponseDto.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(6))
                .status(ru.practicum.shareit.booking.model.Status.REJECTED)
                .build();

        List<BookingResponseDto> bookings = List.of(bookingResponseDto, booking2);

        when(bookingService.getBookingsByOwner(anyLong(), any(State.class)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "PAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].status").value("REJECTED"));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void addBooking_UserNotFound_ReturnsNotFound() throws Exception {
        when(bookingService.addBooking(anyLong(), any(BookingRequestDto.class)))
                .thenThrow(new NotFoundException("User", 999L));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBooking_ItemNotFound_ReturnsNotFound() throws Exception {
        when(bookingService.addBooking(anyLong(), any(BookingRequestDto.class)))
                .thenThrow(new NotFoundException("Item", 999L));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBooking_ItemNotAvailable_ReturnsBadRequest() throws Exception {
        when(bookingService.addBooking(anyLong(), any(BookingRequestDto.class)))
                .thenThrow(new RuntimeException("Item is not available"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_BookingNotFound_ReturnsNotFound() throws Exception {
        when(bookingService.updateStatus(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new NotFoundException("Booking", 999L));

        mockMvc.perform(patch("/bookings/999")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_AlreadyApproved_ReturnsBadRequest() throws Exception {
        when(bookingService.updateStatus(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new RuntimeException("Booking already approved"));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }
}