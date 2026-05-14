package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto createUser(NewUserRequest request);

    UserDto updateUser(Long userId, UpdateUserRequest request);

    List<User> findAll();

    Optional<User> findById(Long id);

    void delete(Long userId);
}
