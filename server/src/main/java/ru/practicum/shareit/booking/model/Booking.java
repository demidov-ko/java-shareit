package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            //Уникальный идентификатор бронирования

    @Column(name = "start_date", nullable = false)
    private LocalDateTime start;    //дата и время начала бронирования

    @Column(name = "end_date", nullable = false)
    private LocalDateTime end;      //дата и время конца бронирования

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;          // вещь, которую пользователь бронирует

    @ManyToOne
    @JoinColumn(name = "booker_id", nullable = false)
    private User booker;        //пользователь, который осуществляет бронирование

    // строковый вид EnumType.STRING. Он сохранит в базу строку, полученную в результате вызова метода enum
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;      //статус бронирования
}
