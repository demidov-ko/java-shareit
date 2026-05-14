package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

// ======================== createItem ========================

    @Test
    void createItem_ValidData_ShouldReturnItemDto() {
        NewItemRequest request = makeNewItemRequest("Дрель", "Мощная дрель", true);
        User owner = makeUser(1L);
        Item item = makeItem(1L, "Дрель", "Мощная дрель", true, owner);
        ItemDto itemDto = makeItemDto(1L, "Дрель", "Мощная дрель", true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.mapToItem(request)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.mapToItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.createItem(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Дрель", result.getName());
        verify(itemRepository).save(item);
    }

    @Test
    void createItem_UserNotFound_ShouldThrowNotFoundException() {
        NewItemRequest request = makeNewItemRequest("Дрель", "Мощная дрель", true);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.createItem(99L, request));
        verify(itemRepository, never()).save(any());
    }

// ======================== updateItem ========================

    @Test
    void updateItem_ValidOwner_ShouldReturnUpdatedDto() {
        User owner = makeUser(1L);
        Item item = makeItem(1L, "Дрель", "Старое описание", true, owner);
        UpdateItemRequest request = makeUpdateItemRequest("Новая дрель", null, null);
        Item updated = makeItem(1L, "Новая дрель", "Старое описание", true, owner);
        ItemDto itemDto = makeItemDto(1L, "Новая дрель", "Старое описание", true);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.updateItemFields(item, request)).thenReturn(updated);
        when(itemRepository.update(updated)).thenReturn(updated);
        when(itemMapper.mapToItemDto(updated)).thenReturn(itemDto);

        ItemDto result = itemService.updateItem(1L, 1L, request);

        assertEquals("Новая дрель", result.getName());
    }

    @Test
    void updateItem_NotOwner_ShouldThrowNotFoundException() {
        User owner = makeUser(1L);
        Item item = makeItem(1L, "Дрель", "Описание", true, owner);
        UpdateItemRequest request = makeUpdateItemRequest("Новая дрель", null, null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(2L, 1L, request));
        verify(itemRepository, never()).update(any());
    }

    @Test
    void updateItem_ItemNotFound_ShouldThrowNotFoundException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(1L, 99L, makeUpdateItemRequest("name", null, null)));
    }

// ======================== getItemById ========================

    @Test
    void getItemById_Exists_ShouldReturnItemDto() {
        User owner = makeUser(1L);
        Item item = makeItem(1L, "Дрель", "Описание", true, owner);
        ItemDto itemDto = makeItemDto(1L, "Дрель", "Описание", true);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.mapToItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.getItemById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getItemById_NotFound_ShouldThrowNotFoundException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.getItemById(99L));
    }

// ======================== getItemsByOwner ========================

    @Test
    void getItemsByOwner_ShouldReturnList() {
        User owner = makeUser(1L);
        Item item = makeItem(1L, "Дрель", "Описание", true, owner);
        ItemDto itemDto = makeItemDto(1L, "Дрель", "Описание", true);

        when(itemRepository.findAllByOwnerId(1L)).thenReturn(List.of(item));
        when(itemMapper.mapToItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.getItemsByOwner(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

// ======================== search ========================

    @Test
    void search_WithText_ShouldReturnMatchingItems() {
        User owner = makeUser(1L);
        Item item = makeItem(1L, "Дрель", "Мощная дрель", true, owner);
        ItemDto itemDto = makeItemDto(1L, "Дрель", "Мощная дрель", true);

        when(itemRepository.search("дрель")).thenReturn(List.of(item));
        when(itemMapper.mapToItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.search("дрель");

        assertEquals(1, result.size());
    }

    @Test
    void search_EmptyText_ShouldReturnEmptyList() {
        when(itemRepository.search("")).thenReturn(List.of());

        List<ItemDto> result = itemService.search("");

        assertTrue(result.isEmpty());
        verify(itemMapper, never()).mapToItemDto(any());
    }

// ======================== helpers ========================

    private NewItemRequest makeNewItemRequest(String name, String description, Boolean available) {
        NewItemRequest r = new NewItemRequest();
        r.setName(name);
        r.setDescription(description);
        r.setAvailable(available);
        return r;
    }

    private UpdateItemRequest makeUpdateItemRequest(String name, String description, Boolean available) {
        UpdateItemRequest r = new UpdateItemRequest();
        r.setName(name);
        r.setDescription(description);
        r.setAvailable(available);
        return r;
    }

    private ItemDto makeItemDto(Long id, String name, String description, Boolean available) {
        ItemDto dto = new ItemDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        return dto;
    }

    private User makeUser(Long id) {
        return User.builder().id(id).name("User").email("user@mail.ru").build();
    }

    private Item makeItem(Long id, String name, String description, Boolean available, User owner) {
        return Item.builder()
                .id(id)
                .name(name)
                .description(description)
                .available(available)
                .owner(owner)
                .build();
    }
}