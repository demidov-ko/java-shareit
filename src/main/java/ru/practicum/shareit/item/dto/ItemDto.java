package ru.practicum.shareit.item.dto;

import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class ItemDto {
    private Long id;            //уникальный идентификатор вещи
    private String name;        //краткое название
    private String description; //развёрнутое описание
    private Boolean available;  //статус о том, доступна или нет вещь для аренды;
    private Long requestId;     //id запроса
}
