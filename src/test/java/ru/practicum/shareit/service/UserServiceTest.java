package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

// ======================== createUser ========================

    @Test
    void createUser_ValidData_ShouldReturnUserDto() {
        NewUserRequest request = makeNewUserRequest("Пользователь", "mail@mail.ru");
        User user = makeUser(1L, "Пользователь", "mail@mail.ru");
        UserDto userDto = makeUserDto(1L, "Пользователь", "mail@mail.ru");

        when(userRepository.existsByEmail("mail@mail.ru")).thenReturn(false);
        when(userMapper.mapToUser(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.mapToUserDto(user)).thenReturn(userDto);

        UserDto result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("mail@mail.ru", result.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void createUser_DuplicateEmail_ShouldThrowConflictException() {
        NewUserRequest request = makeNewUserRequest("Пользователь", "mail@mail.ru");

        when(userRepository.existsByEmail("mail@mail.ru")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

// ======================== updateUser ========================

    @Test
    void updateUser_ValidData_ShouldReturnUpdatedDto() {
        User existing = makeUser(1L, "Старое имя", "old@mail.ru");
        UpdateUserRequest request = makeUpdateUserRequest("Новое имя", "new@mail.ru");
        User updated = makeUser(1L, "Новое имя", "new@mail.ru");
        UserDto userDto = makeUserDto(1L, "Новое имя", "new@mail.ru");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@mail.ru")).thenReturn(false);
        when(userMapper.updateUserFields(existing, request)).thenReturn(updated);
        when(userRepository.save(updated)).thenReturn(updated);
        when(userMapper.mapToUserDto(updated)).thenReturn(userDto);

        UserDto result = userService.updateUser(1L, request);

        assertEquals("Новое имя", result.getName());
        assertEquals("new@mail.ru", result.getEmail());
    }

    @Test
    void updateUser_UserNotFound_ShouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(99L, makeUpdateUserRequest("Имя", "mail@mail.ru")));
    }

    @Test
    void updateUser_DuplicateEmail_ShouldThrowConflictException() {
        User existing = makeUser(1L, "Имя", "old@mail.ru");
        UpdateUserRequest request = makeUpdateUserRequest(null, "existing@mail.ru");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("existing@mail.ru")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> userService.updateUser(1L, request));
        verify(userRepository, never()).save(any());
    }

// ======================== findById ========================

    @Test
    void findById_Exists_ShouldReturnUserDto() {
        User user = makeUser(1L, "Пользователь", "mail@mail.ru");
        UserDto userDto = makeUserDto(1L, "Пользователь", "mail@mail.ru");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.mapToUserDto(user)).thenReturn(userDto);

        Optional<UserDto> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void findById_NotFound_ShouldReturnEmpty() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<UserDto> result = userService.findById(99L);

        assertTrue(result.isEmpty());
    }

// ======================== findAll ========================

    @Test
    void findAll_ShouldReturnAllDtos() {
        List<User> users = List.of(
                makeUser(1L, "Первый", "first@mail.ru"),
                makeUser(2L, "Второй", "second@mail.ru")
        );

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.mapToUserDto(users.get(0)))
                .thenReturn(makeUserDto(1L, "Первый", "first@mail.ru"));
        when(userMapper.mapToUserDto(users.get(1)))
                .thenReturn(makeUserDto(2L, "Второй", "second@mail.ru"));

        List<UserDto> result = userService.findAll();

        assertEquals(2, result.size());
    }

// ======================== delete ========================

    @Test
    void delete_ShouldCallRepository() {
        doNothing().when(userRepository).deleteById(1L);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

// ======================== helpers ========================

    private NewUserRequest makeNewUserRequest(String name, String email) {
        NewUserRequest r = new NewUserRequest();
        r.setName(name);
        r.setEmail(email);
        return r;
    }

    private UpdateUserRequest makeUpdateUserRequest(String name, String email) {
        UpdateUserRequest r = new UpdateUserRequest();
        r.setName(name);
        r.setEmail(email);
        return r;
    }

    private UserDto makeUserDto(Long id, String name, String email) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    private User makeUser(Long id, String name, String email) {
        return User.builder().id(id).name(name).email(email).build();
    }
}
