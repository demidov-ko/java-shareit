package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Long userId, NewItemRequest request);

    ItemDto updateItem(Long userId, Long itemId, UpdateItemRequest request);

    ItemDto getItemById(Long itemId);

    List<ItemDto> getItemsByOwner(Long userId);

    List<ItemDto> search(String text);
}
