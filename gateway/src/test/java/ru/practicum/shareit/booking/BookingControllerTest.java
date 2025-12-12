package ru.practicum.shareit.booking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@DisplayName("Booking Controller tests (gateway module)")
public class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    private BookingRequestDto bookingRequestDto;
    private String bookingRequestJson;

    @BeforeEach
    void setup() throws JsonProcessingException {
        bookingRequestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingRequestJson = objectMapper.writeValueAsString(bookingRequestDto);
    }

    @Test
    void addBooking_ValidData_ReturnsCreated() throws Exception {
        String responseBody = """
                {
                    "id": 1,
                    "start": "2025-01-16T10:00:00",
                    "end": "2025-01-17T10:00:00",
                    "item": {
                        "id": 1,
                        "name": "Item Name"
                    },
                    "booker": {
                        "id": 1,
                        "name": "Booker Name"
                    },
                    "status": "WAITING"
                }
                """;
        ResponseEntity<Object> responseEntity = ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseBody);

        when(bookingClient.addBooking(anyLong(), any(BookingRequestDto.class)))
                .thenReturn(responseEntity);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequestJson))
                .andExpect(status().isCreated())
                .andExpect(content().json(responseBody));
    }

    @Test
    void addBooking_InvalidUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequestJson))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequestJson))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBooking_InvalidBookingData_ReturnsBadRequest() throws Exception {
        BookingRequestDto invalidDtoStartPast = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        String invalidStartJson = objectMapper.writeValueAsString(invalidDtoStartPast);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidStartJson))
                .andExpect(status().isBadRequest());


        BookingRequestDto invalidDtoEndPast = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        try {
            String invalidEndJson = objectMapper.writeValueAsString(invalidDtoEndPast);

            mockMvc.perform(post("/bookings")
                            .header("X-Sharer-User-Id", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidEndJson))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            System.out.println("Exception during serialization: " + e.getMessage());
        }

        BookingRequestDto nullItemIdDto = BookingRequestDto.builder()
                .itemId(null)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        String nullItemIdJson = objectMapper.writeValueAsString(nullItemIdDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nullItemIdJson))
                .andExpect(status().isBadRequest());

        BookingRequestDto nullStartDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(null)
                .end(LocalDateTime.now().plusDays(2))
                .build();

        String nullStartJson = objectMapper.writeValueAsString(nullStartDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nullStartJson))
                .andExpect(status().isBadRequest());

        BookingRequestDto nullEndDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(null)
                .build();

        String nullEndJson = objectMapper.writeValueAsString(nullEndDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nullEndJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_ValidData_ReturnsOk() throws Exception {
        String responseBody = """
                {
                    "id": 1,
                    "status": "APPROVED"
                }
                """;
        ResponseEntity<Object> responseEntity = ResponseEntity.ok(responseBody);

        when(bookingClient.updateStatus(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(responseEntity);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));
    }

    @Test
    void updateStatus_InvalidIds_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 0L)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/bookings/0")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingById_ValidData_ReturnsOk() throws Exception {
        String responseBody = """
                {
                    "id": 1,
                    "start": "2025-01-16T10:00:00",
                    "end": "2025-01-17T10:00:00",
                    "status": "APPROVED"
                }
                """;
        ResponseEntity<Object> responseEntity = ResponseEntity.ok(responseBody);

        when(bookingClient.getBookingById(anyLong(), anyLong()))
                .thenReturn(responseEntity);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));
    }

    @Test
    void getBookingById_InvalidIds_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 0L))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/0")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsByBooker_ValidData_ReturnsOk() throws Exception {
        String responseBody = """
                [
                    {
                        "id": 1,
                        "start": "2025-01-16T10:00:00",
                        "end": "2025-01-17T10:00:00",
                        "status": "APPROVED"
                    },
                    {
                        "id": 2,
                        "start": "2025-01-18T10:00:00",
                        "end": "2025-01-19T10:00:00",
                        "status": "WAITING"
                    }
                ]
                """;
        ResponseEntity<Object> responseEntity = ResponseEntity.ok(responseBody);

        when(bookingClient.getBookingsByBooker(anyLong(), anyString()))
                .thenReturn(responseEntity);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingsByBooker_InvalidUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", -1L)
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 0L)
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsByOwner_ValidData_ReturnsOk() throws Exception {
        String responseBody = """
                [
                    {
                        "id": 1,
                        "start": "2024-01-16T10:00:00",
                        "end": "2024-01-17T10:00:00",
                        "status": "APPROVED"
                    }
                ]
                """;
        ResponseEntity<Object> responseEntity = ResponseEntity.ok(responseBody);

        when(bookingClient.getBookingsByOwner(anyLong(), anyString()))
                .thenReturn(responseEntity);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "CURRENT"))
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingsByOwner_InvalidUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", -1L)
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 0L)
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBooking_EmptyBody_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
