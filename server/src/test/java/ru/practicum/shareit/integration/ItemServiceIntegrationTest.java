package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private UserDto owner;
    private UserDto otherUser;

    @BeforeEach
    void setUp() {
        NewUserRequest ownerRequest = new NewUserRequest();
        ownerRequest.setName("Владелец");
        ownerRequest.setEmail("owner@mail.ru");
        owner = userService.createUser(ownerRequest);

        NewUserRequest otherRequest = new NewUserRequest();
        otherRequest.setName("Другой");
        otherRequest.setEmail("other@mail.ru");
        otherUser = userService.createUser(otherRequest);
    }

    @Test
    void createItem_ShouldSaveAndReturnWithId() {
        NewItemRequest request = new NewItemRequest();
        request.setName("Дрель");
        request.setDescription("Мощная дрель");
        request.setAvailable(true);

        ItemDto result = itemService.createItem(owner.getId(), request);

        assertNotNull(result.getId());
        assertEquals("Дрель", result.getName());
        assertEquals("Мощная дрель", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void updateItem_ShouldUpdateFields() {
        NewItemRequest createRequest = new NewItemRequest();
        createRequest.setName("Дрель");
        createRequest.setDescription("Мощная дрель");
        createRequest.setAvailable(true);
        ItemDto created = itemService.createItem(owner.getId(), createRequest);

        UpdateItemRequest updateRequest = new UpdateItemRequest();
        updateRequest.setName("Новая дрель");
        updateRequest.setAvailable(false);

        ItemDto updated = itemService.updateItem(owner.getId(), created.getId(), updateRequest);

        assertEquals("Новая дрель", updated.getName());
        assertFalse(updated.getAvailable());
    }

    @Test
    void getItemById_ShouldReturnItem() {
        NewItemRequest request = new NewItemRequest();
        request.setName("Дрель");
        request.setDescription("Мощная дрель");
        request.setAvailable(true);
        ItemDto created = itemService.createItem(owner.getId(), request);

        ItemOwnerDto result = itemService.getItemById(owner.getId(), created.getId());

        assertEquals(created.getId(), result.getId());
        assertEquals("Дрель", result.getName());
        assertNotNull(result.getComments());
    }

    @Test
    void getItemsByOwner_ShouldReturnOwnerItems() {
        NewItemRequest r1 = new NewItemRequest();
        r1.setName("Дрель");
        r1.setDescription("Описание");
        r1.setAvailable(true);
        itemService.createItem(owner.getId(), r1);

        NewItemRequest r2 = new NewItemRequest();
        r2.setName("Пила");
        r2.setDescription("Описание");
        r2.setAvailable(true);
        itemService.createItem(owner.getId(), r2);

        List<ItemOwnerDto> result = itemService.getItemsByOwner(owner.getId());

        assertEquals(2, result.size());
    }

    @Test
    void search_ShouldReturnMatchingAvailableItems() {
        NewItemRequest request = new NewItemRequest();
        request.setName("Дрель");
        request.setDescription("Мощная дрель для бетона");
        request.setAvailable(true);
        itemService.createItem(owner.getId(), request);

        List<ItemDto> result = itemService.search("дрель");

        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(i -> i.getName().equals("Дрель")));
    }

    @Test
    void search_EmptyText_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.search("");
        assertTrue(result.isEmpty());
    }
}
