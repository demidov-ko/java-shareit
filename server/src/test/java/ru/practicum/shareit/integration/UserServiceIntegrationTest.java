package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void createUser_ShouldSaveAndReturnWithId() {
        NewUserRequest request = new NewUserRequest();
        request.setName("Иван");
        request.setEmail("ivan@mail.ru");

        UserDto result = userService.createUser(request);

        assertNotNull(result.getId());
        assertEquals("Иван", result.getName());
        assertEquals("ivan@mail.ru", result.getEmail());
    }

    @Test
    void createUser_DuplicateEmail_ShouldThrowConflictException() {
        NewUserRequest request1 = new NewUserRequest();
        request1.setName("Иван");
        request1.setEmail("ivan@mail.ru");
        userService.createUser(request1);

        NewUserRequest request2 = new NewUserRequest();
        request2.setName("Пётр");
        request2.setEmail("ivan@mail.ru");

        assertThrows(ConflictException.class, () -> userService.createUser(request2));
    }

    @Test
    void updateUser_ShouldUpdateFields() {
        NewUserRequest createRequest = new NewUserRequest();
        createRequest.setName("Иван");
        createRequest.setEmail("ivan@mail.ru");
        UserDto created = userService.createUser(createRequest);

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Пётр");
        updateRequest.setEmail("petr@mail.ru");

        UserDto updated = userService.updateUser(created.getId(), updateRequest);

        assertEquals("Пётр", updated.getName());
        assertEquals("petr@mail.ru", updated.getEmail());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        NewUserRequest r1 = new NewUserRequest();
        r1.setName("Иван");
        r1.setEmail("ivan@mail.ru");
        userService.createUser(r1);

        NewUserRequest r2 = new NewUserRequest();
        r2.setName("Пётр");
        r2.setEmail("petr@mail.ru");
        userService.createUser(r2);

        List<UserDto> result = userService.findAll();

        assertTrue(result.size() >= 2);
    }

    @Test
    void findById_ShouldReturnUser() {
        NewUserRequest request = new NewUserRequest();
        request.setName("Иван");
        request.setEmail("ivan@mail.ru");
        UserDto created = userService.createUser(request);

        Optional<UserDto> result = userService.findById(created.getId());

        assertTrue(result.isPresent());
        assertEquals(created.getId(), result.get().getId());
    }

    @Test
    void delete_ShouldRemoveUser() {
        NewUserRequest request = new NewUserRequest();
        request.setName("Иван");
        request.setEmail("ivan@mail.ru");
        UserDto created = userService.createUser(request);

        userService.delete(created.getId());

        Optional<UserDto> result = userService.findById(created.getId());
        assertTrue(result.isEmpty());
    }
}
