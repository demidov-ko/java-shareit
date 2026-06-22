package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "request", ignore = true)
    Item mapToItem(NewItemRequest request);

    @Mapping(target = "requestId", source = "request.id")
    @Mapping(target = "comments", ignore = true)
    ItemDto mapToItemDto(Item item);

    default Item updateItemFields(Item item, UpdateItemRequest request) {
        return ItemMapperHelper.updateItemFields(item, request);
    }
}
