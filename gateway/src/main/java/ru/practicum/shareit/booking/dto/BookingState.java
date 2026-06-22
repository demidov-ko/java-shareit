package ru.practicum.shareit.booking.dto;


public enum BookingState {
    ALL,        //все бронирования
    CURRENT,    //текущие бронирования
    PAST,       //завершенные бронирования
    FUTURE,     //будщие бронирования
    WAITING,   //ожидающие бронирования
    REJECTED;    //отклонённые бронирования
}
