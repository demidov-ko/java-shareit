package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody NewItemRequest request) {
        return ResponseEntity.status(201).body(itemService.createItem(userId, request));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemRequest request) {
        return ResponseEntity.ok(itemService.updateItem(userId, itemId, request));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.getItemById(itemId));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok(itemService.getItemsByOwner(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(@RequestParam String text) {
        return ResponseEntity.ok(itemService.search(text));
    }
}
