package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

// ======================== POST /bookings ========================

    @Test
    void createBooking_ValidData_ShouldReturn201() throws Exception {
        NewBookingRequest request = makeRequest(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        BookingDto bookingDto = makeBookingDto(1L, Status.WAITING);

        when(bookingService.createBooking(eq(1L), any(NewBookingRequest.class)))
                .thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void createBooking_NoHeader_ShouldReturn400() throws Exception {
        NewBookingRequest request = makeRequest(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_ItemNotFound_ShouldReturn404() throws Exception {
        NewBookingRequest request = makeRequest(99L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        when(bookingService.createBooking(eq(1L), any(NewBookingRequest.class)))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_ItemNotAvailable_ShouldReturn400() throws Exception {
        NewBookingRequest request = makeRequest(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        when(bookingService.createBooking(eq(1L), any(NewBookingRequest.class)))
                .thenThrow(new BadRequestException("Вещь недоступна"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

// ======================== PATCH /bookings/{bookingId} ========================

    @Test
    void approveBooking_Approve_ShouldReturn200() throws Exception {
        BookingDto bookingDto = makeBookingDto(1L, Status.APPROVED);

        when(bookingService.approveBooking(eq(1L), eq(1L), eq(true)))
                .thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approveBooking_Reject_ShouldReturn200() throws Exception {
        BookingDto bookingDto = makeBookingDto(1L, Status.REJECTED);

        when(bookingService.approveBooking(eq(1L), eq(1L), eq(false)))
                .thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void approveBooking_NotOwner_ShouldReturn403() throws Exception {
        when(bookingService.approveBooking(eq(99L), eq(1L), eq(true)))
                .thenThrow(new ForbiddenException("Нет доступа"));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 99L)
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

// ======================== GET /bookings/{bookingId} ========================

    @Test
    void getBookingById_ShouldReturn200() throws Exception {
        BookingDto bookingDto = makeBookingDto(1L, Status.WAITING);

        when(bookingService.getBookingById(eq(1L), eq(1L)))
                .thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getBookingById_NotFound_ShouldReturn404() throws Exception {
        when(bookingService.getBookingById(eq(1L), eq(99L)))
                .thenThrow(new NotFoundException("Бронирование не найдено"));

        mockMvc.perform(get("/bookings/99")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingById_NoAccess_ShouldReturn403() throws Exception {
        when(bookingService.getBookingById(eq(99L), eq(1L)))
                .thenThrow(new ForbiddenException("Нет доступа"));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 99L))
                .andExpect(status().isForbidden());
    }

// ======================== GET /bookings ========================

    @Test
    void getBookingsByBooker_ShouldReturn200() throws Exception {
        BookingDto bookingDto = makeBookingDto(1L, Status.WAITING);

        when(bookingService.getBookingsByBooker(eq(1L), eq("ALL")))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getBookingsByBooker_InvalidState_ShouldReturn400() throws Exception {
        when(bookingService.getBookingsByBooker(eq(1L), eq("INVALID")))
                .thenThrow(new BadRequestException("Неизвестный статус: INVALID"));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest());
    }

// ======================== GET /bookings/owner ========================

    @Test
    void getBookingsByOwner_ShouldReturn200() throws Exception {
        BookingDto bookingDto = makeBookingDto(1L, Status.WAITING);

        when(bookingService.getBookingsByOwner(eq(1L), eq("ALL")))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getBookingsByOwner_InvalidState_ShouldReturn400() throws Exception {
        when(bookingService.getBookingsByOwner(eq(1L), eq("INVALID")))
                .thenThrow(new BadRequestException("Неизвестный статус: INVALID"));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest());
    }

// ======================== helpers ========================

    private NewBookingRequest makeRequest(Long itemId,
                                          LocalDateTime start,
                                          LocalDateTime end) {
        NewBookingRequest r = new NewBookingRequest();
        r.setItemId(itemId);
        r.setStart(start);
        r.setEnd(end);
        return r;
    }

    private BookingDto makeBookingDto(Long id, Status status) {
        BookingDto dto = new BookingDto();
        dto.setId(id);
        dto.setStatus(status);
        return dto;
    }
}
