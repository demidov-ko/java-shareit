package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;

public class ItemMapperHelper {
    public static Item updateItemFields(Item item, UpdateItemRequest request) {
        if (request.hasName()) {
            item.setName(request.getName());
        }

        if (request.hasDescription()) {
            item.setDescription(request.getDescription());
        }

        if (request.hasAvailable()) {
            item.setAvailable(request.getAvailable());
        }

        return item;
    }
}
