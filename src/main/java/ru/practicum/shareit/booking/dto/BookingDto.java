package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */
@Data
public class BookingDto {
    private Long id;            //Уникальный идентификатор бронирования
    private LocalDate start;    //дата и время начала бронирования
    private LocalDate end;      //дата и время конца бронирования
    private Item item;          // вещь, которую пользователь бронирует
    private User booker;        //пользователь, который осуществляет бронирование
    private Status status;      //статус бронирования
}
