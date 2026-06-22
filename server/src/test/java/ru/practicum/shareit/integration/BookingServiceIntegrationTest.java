package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private UserDto owner;
    private UserDto booker;
    private ItemDto item;

    @BeforeEach
    void setUp() {
        NewUserRequest ownerRequest = new NewUserRequest();
        ownerRequest.setName("Владелец");
        ownerRequest.setEmail("owner@mail.ru");
        owner = userService.createUser(ownerRequest);

        NewUserRequest bookerRequest = new NewUserRequest();
        bookerRequest.setName("Арендатор");
        bookerRequest.setEmail("booker@mail.ru");
        booker = userService.createUser(bookerRequest);

        NewItemRequest itemRequest = new NewItemRequest();
        itemRequest.setName("Дрель");
        itemRequest.setDescription("Мощная дрель");
        itemRequest.setAvailable(true);
        item = itemService.createItem(owner.getId(), itemRequest);
    }

    @Test
    void createBooking_ShouldSaveWithWaitingStatus() {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto result = bookingService.createBooking(booker.getId(), request);

        assertNotNull(result.getId());
        assertEquals(Status.WAITING, result.getStatus());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
    }

    @Test
    void approveBooking_ShouldChangeStatusToApproved() {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto booking = bookingService.createBooking(booker.getId(), request);

        BookingDto result = bookingService.approveBooking(
                owner.getId(), booking.getId(), true);

        assertEquals(Status.APPROVED, result.getStatus());
    }

    @Test
    void approveBooking_Reject_ShouldChangeStatusToRejected() {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto booking = bookingService.createBooking(booker.getId(), request);

        BookingDto result = bookingService.approveBooking(
                owner.getId(), booking.getId(), false);

        assertEquals(Status.REJECTED, result.getStatus());
    }

    @Test
    void getBookingById_ShouldReturnBooking() {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto booking = bookingService.createBooking(booker.getId(), request);

        BookingDto result = bookingService.getBookingById(booker.getId(), booking.getId());

        assertEquals(booking.getId(), result.getId());
        assertEquals(Status.WAITING, result.getStatus());
    }

    @Test
    void getBookingsByBooker_ShouldReturnBookerBookings() {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.createBooking(booker.getId(), request);

        List<BookingDto> result = bookingService.getBookingsByBooker(
                booker.getId(), "ALL");

        assertFalse(result.isEmpty());
        assertTrue(result.stream()
                .allMatch(b -> b.getBooker().getId().equals(booker.getId())));
    }

    @Test
    void getBookingsByOwner_ShouldReturnOwnerBookings() {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.createBooking(booker.getId(), request);

        List<BookingDto> result = bookingService.getBookingsByOwner(
                owner.getId(), "ALL");

        assertFalse(result.isEmpty());
        assertTrue(result.stream()
                .allMatch(b -> b.getItem().getId().equals(item.getId())));
    }
}
