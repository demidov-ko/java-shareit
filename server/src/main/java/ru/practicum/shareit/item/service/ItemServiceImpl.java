package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    @Override
    public ItemDto createItem(Long userId, NewItemRequest request) {
        log.info("Создание вещи: name={}", request.getName());

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemMapper.mapToItem(request);

        if (request.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(request.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));

            item.setRequest(itemRequest);
        }

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
        return itemMapper.mapToItemDto(itemRepository.save(updated));
    }

    @Override
    public ItemOwnerDto getItemById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        LocalDateTime now = LocalDateTime.now();
        List<Comment> comments = commentRepository.findAllByItemId(itemId);

        log.info("Комментарии для вещи {}: {}", itemId, comments.size());

        boolean isOwner = item.getOwner().getId().equals(userId);
        //владелец вещи видит даты бронирования
        if (isOwner) {
            List<Booking> bookings = bookingRepository.findByItemIdIn(
                    List.of(itemId), SORT_BY_START_DESC);
            return buildItemOwnerDto(item, bookings, comments, now);
        }

        return buildItemOwnerDto(item, List.of(), comments, now);
    }

    @Override
    public List<ItemOwnerDto> getItemsByOwner(Long userId) {
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        //группируем бронирования по itemId
        Map<Long, List<Booking>> bookingsByItemId = bookingRepository
                .findByItemIdIn(itemIds, SORT_BY_START_DESC)
                .stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        //группируем комментарии по itemId
        Map<Long, List<Comment>> commentsByItemId = commentRepository
                .findAllByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> buildItemOwnerDto(
                        item,
                        bookingsByItemId.getOrDefault(item.getId(), List.of()),
                        commentsByItemId.getOrDefault(item.getId(), List.of()),
                        now))
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.search(text).stream()
                .map(itemMapper::mapToItemDto)
                .toList();
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, NewCommentRequest request) {
        log.info("Добавление комментария");

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        // проверяем что пользователь брал вещь в аренду и аренда завершена
        boolean hasCompletedBooking = bookingRepository
                .findByBookerIdAndItemIdAndStatusAndEndBefore(
                        userId, itemId, Status.APPROVED, LocalDateTime.now()).isPresent();

        if (!hasCompletedBooking) {
            throw new BadRequestException("Оставить отзыв можно только после завершения аренды");
        }

        Comment comment = Comment.builder()
                .text(request.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(comment);
        log.info("Комментарий добавлен: id={}", saved.getId());
        return commentMapper.mapToCommentDto(saved);
    }

    //собираем ItemOwnerDto для одной вещи
    private ItemOwnerDto buildItemOwnerDto(Item item,
                                           List<Booking> itemBookings,
                                           List<Comment> itemComments,
                                           LocalDateTime now) {

        ItemOwnerDto dto = new ItemOwnerDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());

        //фильтруем только APPROVED бронирования
        List<Booking> aproved = itemBookings.stream()
                .filter(b -> b.getStatus().equals(Status.APPROVED))
                .toList();

        //последнее бронирование — максимальное end до now
        aproved.stream()
                .filter(b -> b.getEnd().isBefore(now))
                .max(Comparator.comparing(Booking::getEnd))
                .ifPresent(b -> dto.setLastBooking(b.getEnd()));

        //ближайшее следующее — минимальное start после now
        aproved.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .ifPresent(b -> dto.setNextBooking(b.getStart()));

        //комментарии для этой вещи
        List<CommentDto> comments = itemComments.stream()
                .map(commentMapper::mapToCommentDto)
                .toList();
        dto.setComments(comments);

        return dto;
    }
}
