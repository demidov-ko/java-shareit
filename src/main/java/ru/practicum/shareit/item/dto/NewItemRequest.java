package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewItemRequest {
    @NotBlank(message = "Название не может быть пустым")
    private String name;        //краткое название

    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 200, message = "Описание не может быть длиннее 200 символов")
    private String description; //развёрнутое описание

    @NotNull(message = "Статус доступности обязателен")
    private Boolean available;  //статус о том, доступна или нет вещь для аренды;
}
