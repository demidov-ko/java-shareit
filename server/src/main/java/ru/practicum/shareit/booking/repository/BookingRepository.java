package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    //все бронирования пользователя
    //(Sort передается посл. параметром в метод, а Spring воспринимает его как ORDER BY в SQL)
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    //текущие бронирования пользователя
    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(
            Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    //завершённые бронирования пользователя
    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    //будущие бронирования пользователя
    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    //бронирования пользователя по статусу
    List<Booking> findByBookerIdAndStatus(Long bookerId, Status status, Sort sort);

    List<Booking> findByBookerIdAndItemId(Long bookerId, Long itemId);

    //----------------------------------------------------------------------------------------

    //все бронирования для вещей пользователя
    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    //текущие бронирования для вещей  пользователя
    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(
            Long ownerId, LocalDateTime start, LocalDateTime end, Sort sort);

    //завершённые бронирования для вещей пользователя
    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime end, Sort sort);

    //будущие бронирования для вещей пользователя
    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime start, Sort sort);

    //бронирования для вещей пользователя по статусу
    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, Status status, Sort sort);

    //все бронирования для списка вещей
    List<Booking> findByItemIdIn(List<Long> itemIds, Sort sort);

    //проверка, что пользователь брал вещь
    Optional<Booking> findByBookerIdAndItemIdAndStatusAndEndBefore(
            Long bookerId, Long itemId, Status status, LocalDateTime end);
}
