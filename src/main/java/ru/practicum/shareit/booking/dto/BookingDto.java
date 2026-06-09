package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
public class BookingDto {
    private Long id;            //Уникальный идентификатор бронирования
    private LocalDateTime start;    //дата и время начала бронирования
    private LocalDateTime end;      //дата и время конца бронирования
    private ItemDto item;          // вещь, которую пользователь бронирует
    private UserDto booker;        //пользователь, который осуществляет бронирование
    private Status status;      //статус бронирования
}
