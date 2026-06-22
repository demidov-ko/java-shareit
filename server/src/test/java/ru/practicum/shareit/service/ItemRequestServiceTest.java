package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestRequest;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void create_ShouldReturnDto() {
        User user = User.builder()
                .id(1L)
                .name("User")
                .email("user@mail.ru")
                .build();

        NewItemRequestRequest request = new NewItemRequestRequest();
        request.setDescription("Нужна дрель");

        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель")
                .requestor(user)
                .build();

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any())).thenReturn(itemRequest);
        when(itemRequestMapper.toDto(any())).thenReturn(dto);

        ItemRequestDto result = itemRequestService.create(1L, request);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void create_UserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NewItemRequestRequest request = new NewItemRequestRequest();
        assertThrows(NotFoundException.class, () -> itemRequestService.create(1L, request));
    }

    @Test
    void getRequest_ShouldReturnDto() {
        User user = User.builder().id(1L).build();
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .build();

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRequestMapper.toDto(request)).thenReturn(dto);

        ItemRequestDto result = itemRequestService.getRequest(1L, 1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void getRequest_RequestNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequest(1L, 1L));
    }

    @Test
    void getOwnRequests_ShouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(ItemRequest.builder().id(1L).build()));
        when(itemRequestMapper.toDto(any())).thenReturn(new ItemRequestDto());

        List<ItemRequestDto> result = itemRequestService.getOwnRequests(1L);
        assertEquals(1, result.size());
    }

    @Test
    void getAllRequests_ShouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(1L))
                .thenReturn(List.of(ItemRequest.builder().id(1L).build()));
        when(itemRequestMapper.toDto(any())).thenReturn(new ItemRequestDto());

        List<ItemRequestDto> result = itemRequestService.getAllRequests(1L);
        assertEquals(1, result.size());
    }
}
