package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewBookingRequest {
    @FutureOrPresent
    @NotNull(message = "Дата и время начала бронирования не могут быть пустыми")
    private LocalDateTime start;    //дата и время начала бронирования

    @Future
    @NotNull(message = "Дата и время конца бронирования не могут быть пустыми")
    private LocalDateTime end;      //дата и время конца бронирования

    @NotNull(message = "id вещи не может быть пустым")
    private Long itemId;
}
