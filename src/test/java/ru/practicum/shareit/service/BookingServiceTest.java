package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

// ======================== createBooking ========================

    @Test
    void createBooking_ValidData_ShouldReturnBookingDto() {
        User booker = makeUser(1L);
        User owner = makeUser(2L);
        Item item = makeItem(1L, true, owner);
        NewBookingRequest request = makeRequest(item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        Booking booking = makeBooking(1L, item, booker, Status.WAITING);
        BookingDto bookingDto = makeBookingDto(1L, Status.WAITING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.mapToBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.createBooking(1L, request);

        assertNotNull(result);
        assertEquals(Status.WAITING, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_UserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(99L, makeRequest(1L,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2))));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ItemNotFound_ShouldThrowNotFoundException() {
        User booker = makeUser(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(1L, makeRequest(99L,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2))));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ItemNotAvailable_ShouldThrowBadRequestException() {
        User booker = makeUser(1L);
        User owner = makeUser(2L);
        Item item = makeItem(1L, false, owner); // недоступна

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(1L, makeRequest(1L,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2))));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_OwnerBookingOwnItem_ShouldThrowForbiddenException() {
        User owner = makeUser(1L);
        Item item = makeItem(1L, true, owner);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class,
                () -> bookingService.createBooking(1L, makeRequest(1L,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2))));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_EndBeforeStart_ShouldThrowBadRequestException() {
        User booker = makeUser(1L);
        User owner = makeUser(2L);
        Item item = makeItem(1L, true, owner);

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(1L, makeRequest(1L,
                        LocalDateTime.now().plusDays(2),
                        LocalDateTime.now().plusDays(1)))); // end раньше start
        verify(bookingRepository, never()).save(any());
    }

// ======================== approveBooking ========================

    @Test
    void approveBooking_Approve_ShouldReturnApprovedDto() {
        User owner = makeUser(1L);
        Item item = makeItem(1L, true, owner);
        Booking booking = makeBooking(1L, item, makeUser(2L), Status.WAITING);
        BookingDto bookingDto = makeBookingDto(1L, Status.APPROVED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.mapToBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.approveBooking(1L, 1L, true);

        assertEquals(Status.APPROVED, result.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void approveBooking_Reject_ShouldReturnRejectedDto() {
        User owner = makeUser(1L);
        Item item = makeItem(1L, true, owner);
        Booking booking = makeBooking(1L, item, makeUser(2L), Status.WAITING);
        BookingDto bookingDto = makeBookingDto(1L, Status.REJECTED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.mapToBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.approveBooking(1L, 1L, false);

        assertEquals(Status.REJECTED, result.getStatus());
    }

    @Test
    void approveBooking_NotOwner_ShouldThrowForbiddenException() {
        User owner = makeUser(1L);
        Item item = makeItem(1L, true, owner);
        Booking booking = makeBooking(1L, item, makeUser(2L), Status.WAITING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ForbiddenException.class,
                () -> bookingService.approveBooking(99L, 1L, true));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_AlreadyApproved_ShouldThrowBadRequestException() {
        User owner = makeUser(1L);
        Item item = makeItem(1L, true, owner);
        Booking booking = makeBooking(1L, item, makeUser(2L), Status.APPROVED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BadRequestException.class,
                () -> bookingService.approveBooking(1L, 1L, true));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_NotFound_ShouldThrowNotFoundException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(1L, 99L, true));
    }

// ======================== getBookingById ========================

    @Test
    void getBookingById_ByBooker_ShouldReturnDto() {
        User booker = makeUser(1L);
        User owner = makeUser(2L);
        Item item = makeItem(1L, true, owner);
        Booking booking = makeBooking(1L, item, booker, Status.WAITING);
        BookingDto bookingDto = makeBookingDto(1L, Status.WAITING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.mapToBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.getBookingById(1L, 1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getBookingById_ByOwner_ShouldReturnDto() {
        User booker = makeUser(1L);
        User owner = makeUser(2L);
        Item item = makeItem(1L, true, owner);
        Booking booking = makeBooking(1L, item, booker, Status.WAITING);
        BookingDto bookingDto = makeBookingDto(1L, Status.WAITING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.mapToBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.getBookingById(2L, 1L); // owner

        assertEquals(1L, result.getId());
    }

    @Test
    void getBookingById_NotOwnerNotBooker_ShouldThrowForbiddenException() {
        User booker = makeUser(1L);
        User owner = makeUser(2L);
        Item item = makeItem(1L, true, owner);
        Booking booking = makeBooking(1L, item, booker, Status.WAITING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ForbiddenException.class,
                () -> bookingService.getBookingById(99L, 1L));
    }

    @Test
    void getBookingById_NotFound_ShouldThrowNotFoundException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(1L, 99L));
    }

// ======================== getBookingsByBooker ========================

    @Test
    void getBookingsByBooker_AllState_ShouldReturnList() {
        User booker = makeUser(1L);
        Booking booking = makeBooking(1L, makeItem(1L, true, makeUser(2L)),
                booker, Status.WAITING);
        BookingDto bookingDto = makeBookingDto(1L, Status.WAITING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerId(eq(1L), any()))
                .thenReturn(List.of(booking));
        when(bookingMapper.mapToBookingDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookingsByBooker(1L, "ALL");

        assertEquals(1, result.size());
    }

    @Test
    void getBookingsByBooker_InvalidState_ShouldThrowBadRequestException() {
        User booker = makeUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));

        assertThrows(BadRequestException.class,
                () -> bookingService.getBookingsByBooker(1L, "INVALID"));
    }

    @Test
    void getBookingsByBooker_UserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingsByBooker(99L, "ALL"));
    }

// ======================== getBookingsByOwner ========================

    @Test
    void getBookingsByOwner_AllState_ShouldReturnList() {
        User owner = makeUser(1L);
        Booking booking = makeBooking(1L, makeItem(1L, true, owner),
                makeUser(2L), Status.WAITING);
        BookingDto bookingDto = makeBookingDto(1L, Status.WAITING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerId(eq(1L), any()))
                .thenReturn(List.of(booking));
        when(bookingMapper.mapToBookingDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookingsByOwner(1L, "ALL");

        assertEquals(1, result.size());
    }

    @Test
    void getBookingsByOwner_InvalidState_ShouldThrowBadRequestException() {
        User owner = makeUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThrows(BadRequestException.class,
                () -> bookingService.getBookingsByOwner(1L, "INVALID"));
    }

    @Test
    void getBookingsByOwner_UserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingsByOwner(99L, "ALL"));
    }

// ======================== helpers ========================

    private User makeUser(Long id) {
        return User.builder()
                .id(id)
                .name("User " + id)
                .email("user" + id + "@mail.ru")
                .build();
    }

    private Item makeItem(Long id, Boolean available, User owner) {
        return Item.builder()
                .id(id)
                .name("Item " + id)
                .description("Description " + id)
                .available(available)
                .owner(owner)
                .build();
    }

    private Booking makeBooking(Long id, Item item, User booker, Status status) {
        return Booking.builder()
                .id(id)
                .item(item)
                .booker(booker)
                .status(status)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
    }

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
