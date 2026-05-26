package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto createItem(Long userId, NewItemRequest request) {
        log.info("Создание вещи: name={}", request.getName());

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemMapper.mapToItem(request);
        item.setOwner(owner);

        log.info("Вещь успешно создана: id={}", item.getId());
        return itemMapper.mapToItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, UpdateItemRequest request) {
        log.info("Обновление вещи с id={}", itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Вещь не найдена у данного пользователя");
        }

        Item updated = itemMapper.updateItemFields(item, request);

        log.info("Вещь успешно обновлёна: id={}", updated.getId());
        return itemMapper.mapToItemDto(itemRepository.update(updated));
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .map(itemMapper::mapToItemDto)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        return itemRepository.findAllByOwnerId(userId).stream()
                .map(itemMapper::mapToItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemRepository.search(text).stream()
                .map(itemMapper::mapToItemDto)
                .toList();
    }
}
