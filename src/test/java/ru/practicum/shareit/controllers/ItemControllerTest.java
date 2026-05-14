package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @MockBean
    private ItemMapper itemMapper;

// ======================== POST /items ========================

    @Test
    void createItem_ValidData_ShouldReturn201() throws Exception {
        NewItemRequest request = makeNewItemRequest("Дрель", "Мощная дрель", true);
        ItemDto itemDto = makeItemDto(1L, "Дрель", "Мощная дрель", true);

        when(itemService.createItem(eq(1L), any(NewItemRequest.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description").value("Мощная дрель"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void createItem_NoName_ShouldReturn400() throws Exception {
        NewItemRequest request = makeNewItemRequest(null, "Мощная дрель", true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_NoAvailable_ShouldReturn400() throws Exception {
        NewItemRequest request = makeNewItemRequest("Дрель", "Мощная дрель", null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_NoHeader_ShouldReturn400() throws Exception {
        NewItemRequest request = makeNewItemRequest("Дрель", "Мощная дрель", true);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_UserNotFound_ShouldReturn404() throws Exception {
        NewItemRequest request = makeNewItemRequest("Дрель", "Мощная дрель", true);

        when(itemService.createItem(eq(99L), any(NewItemRequest.class)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

// ======================== PATCH /items/{itemId} ========================

    @Test
    void updateItem_ValidData_ShouldReturn200() throws Exception {
        UpdateItemRequest request = makeUpdateItemRequest("Новая дрель", null, null);
        ItemDto itemDto = makeItemDto(1L, "Новая дрель", "Мощная дрель", true);

        when(itemService.updateItem(eq(1L), eq(1L), any(UpdateItemRequest.class)))
                .thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новая дрель"));
    }

    @Test
    void updateItem_NotOwner_ShouldReturn404() throws Exception {
        UpdateItemRequest request = makeUpdateItemRequest("Новая дрель", null, null);

        when(itemService.updateItem(eq(2L), eq(1L), any(UpdateItemRequest.class)))
                .thenThrow(new NotFoundException("Вещь не найдена у данного пользователя"));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

// ======================== GET /items/{itemId} ========================

    @Test
    void getItemById_Exists_ShouldReturn200() throws Exception {
        ItemDto itemDto = makeItemDto(1L, "Дрель", "Мощная дрель", true);

        when(itemService.getItemById(1L)).thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void getItemById_NotFound_ShouldReturn404() throws Exception {
        when(itemService.getItemById(99L))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        mockMvc.perform(get("/items/99")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

// ======================== GET /items ========================

    @Test
    void getItemsByOwner_ShouldReturn200() throws Exception {
        ItemDto itemDto = makeItemDto(1L, "Дрель", "Мощная дрель", true);

        when(itemService.getItemsByOwner(1L)).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

// ======================== GET /items/search ========================

    @Test
    void search_WithText_ShouldReturn200() throws Exception {
        ItemDto itemDto = makeItemDto(1L, "Дрель", "Мощная дрель", true);

        when(itemService.search("дрель")).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void search_EmptyText_ShouldReturnEmptyList() throws Exception {
        when(itemService.search("")).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
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
}
