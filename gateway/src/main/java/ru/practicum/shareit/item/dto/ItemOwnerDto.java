package ru.practicum.shareit.item.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemOwnerDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
    private List<CommentDto> comments; //отзывы
    private LocalDateTime lastBooking; //дата посл. бронирования
    private LocalDateTime nextBooking; //дата след. бронирования
}
