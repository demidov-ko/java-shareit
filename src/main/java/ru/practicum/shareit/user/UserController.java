package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody NewUserRequest request) {
        UserDto userDto = userService.createUser(request);
        return ResponseEntity.status(201).body(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDto updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.findAll();
        List<UserDto> userDtos = users.stream()
                .map(userMapper::mapToUserDto)
                .toList();
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        UserDto userDto = userService.findById(userId)
                .map(userMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return ResponseEntity.ok(userDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
