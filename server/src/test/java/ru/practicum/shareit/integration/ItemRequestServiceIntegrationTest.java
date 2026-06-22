package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    private UserDto requester;
    private UserDto anotherUser;

    @BeforeEach
    void setUp() {

        NewUserRequest first = new NewUserRequest();
        first.setName("Requester");
        first.setEmail("requester@mail.ru");

        requester = userService.createUser(first);

        NewUserRequest second = new NewUserRequest();
        second.setName("Another");
        second.setEmail("another@mail.ru");

        anotherUser = userService.createUser(second);
    }

    @Test
    void create_ShouldSaveRequest() {

        NewItemRequestRequest request = new NewItemRequestRequest();
        request.setDescription("Нужна дрель");

        ItemRequestDto result =
                itemRequestService.create(requester.getId(), request);

        assertNotNull(result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertNotNull(result.getCreated());
    }

    @Test
    void getRequest_ShouldReturnSavedRequest() {

        NewItemRequestRequest request = new NewItemRequestRequest();
        request.setDescription("Нужна пила");

        ItemRequestDto created =
                itemRequestService.create(requester.getId(), request);

        ItemRequestDto result =
                itemRequestService.getRequest(
                        requester.getId(),
                        created.getId());

        assertEquals(created.getId(), result.getId());
        assertEquals("Нужна пила", result.getDescription());
    }

    @Test
    void getOwnRequests_ShouldReturnOnlyOwnRequests() {

        NewItemRequestRequest request1 = new NewItemRequestRequest();
        request1.setDescription("Нужна дрель");

        NewItemRequestRequest request2 = new NewItemRequestRequest();
        request2.setDescription("Нужна пила");

        itemRequestService.create(requester.getId(), request1);
        itemRequestService.create(requester.getId(), request2);

        List<ItemRequestDto> result =
                itemRequestService.getOwnRequests(requester.getId());

        assertEquals(2, result.size());
    }

    @Test
    void getAllRequests_ShouldReturnRequestsOfOtherUsers() {

        NewItemRequestRequest request = new NewItemRequestRequest();
        request.setDescription("Нужен шуруповерт");

        itemRequestService.create(requester.getId(), request);

        List<ItemRequestDto> result =
                itemRequestService.getAllRequests(anotherUser.getId());

        assertEquals(1, result.size());
        assertEquals(
                "Нужен шуруповерт",
                result.get(0).getDescription()
        );
    }
}
