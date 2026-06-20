package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestRequest;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, NewItemRequestRequest request);

    ItemRequestDto getRequest(Long userId, Long requestId);

    List<ItemRequestDto> getAllRequests(Long userId);

    List<ItemRequestDto> getOwnRequests(Long userId);
}
