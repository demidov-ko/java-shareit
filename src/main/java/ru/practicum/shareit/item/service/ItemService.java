package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Long userId, NewItemRequest request);

    ItemDto updateItem(Long userId, Long itemId, UpdateItemRequest request);

    ItemOwnerDto getItemById(Long itemId);

    List<ItemOwnerDto> getItemsByOwner(Long userId);

    List<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, NewCommentRequest request);
}
