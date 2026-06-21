package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    private static final Sort SORT_BY_START_DESC =
            Sort.by(Sort.Direction.DESC, "start");

    @Override
    public BookingDto createBooking(Long userId, NewBookingRequest request) {
        log.info("Создание бронирования для вещи: id={}", request.getItemId());
        log.info("SERVER TIME on create: {}", LocalDateTime.now());
        log.info("Request start: {}, end: {}", request.getStart(), request.getEnd());

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Владелец не может бронировать свою вещь");
        }

        if (!request.getEnd().isAfter(request.getStart())) {
            throw new BadRequestException("Дата окончания бронирования должна быть позже даты начала");
        }

        Booking booking = Booking.builder()
                .start(request.getStart())
                .end(request.getEnd())
                .item(item)
                .booker(booker)
                .status(Status.WAITING)
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Бронирование успешно создано: id={}", saved.getId());

        return bookingMapper.mapToBookingDto(saved);
    }

    @Override
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Подтверждать бронирование может только хозяин вещи");
        }

        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new BadRequestException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        Booking saved = bookingRepository.save(booking);
        log.info("Для бронирование id={} изменен статус на {}", bookingId, saved.getStatus());

        return bookingMapper.mapToBookingDto(saved);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new ForbiddenException("Нет доступа к данным бронирования");
        }

        return bookingMapper.mapToBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByBooker(Long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        BookingState bookingState = parseState(state);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findByBookerId(userId, SORT_BY_START_DESC);
            case CURRENT -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(
                    userId, now, now, SORT_BY_START_DESC);
            case PAST -> bookingRepository.findByBookerIdAndEndBefore(
                    userId, now, SORT_BY_START_DESC);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfter(
                    userId, now, SORT_BY_START_DESC);
            case WAITING -> bookingRepository.findByBookerIdAndStatus(
                    userId, Status.WAITING, SORT_BY_START_DESC);
            case REJECTED -> bookingRepository.findByBookerIdAndStatus(
                    userId, Status.REJECTED, SORT_BY_START_DESC);
        };

        return bookings.stream()
                .map(bookingMapper::mapToBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> getBookingsByOwner(Long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        BookingState bookingState = parseState(state);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findByItemOwnerId(userId, SORT_BY_START_DESC);
            case CURRENT -> bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(
                    userId, now, now, SORT_BY_START_DESC);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBefore(
                    userId, now, SORT_BY_START_DESC);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartAfter(
                    userId, now, SORT_BY_START_DESC);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatus(
                    userId, Status.WAITING, SORT_BY_START_DESC);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatus(
                    userId, Status.REJECTED, SORT_BY_START_DESC);
        };

        return bookings.stream()
                .map(bookingMapper::mapToBookingDto)
                .toList();
    }

    private BookingState parseState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Неизвестный статус: " + state);
        }
    }
}
