package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(nextId++);
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAllByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .toList();
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String lower = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item ->
                        item.getName().toLowerCase().contains(lower) ||
                                item.getDescription().toLowerCase().contains(lower))
                .toList();
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }
}
