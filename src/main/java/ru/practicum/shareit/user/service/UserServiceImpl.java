package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(NewUserRequest request) {
        log.info("Создание пользователя: email={}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        User user = userMapper.mapToUser(request);
        User savedUser = userRepository.save(user);
        log.info("Пользователь успешно создан: id={}", savedUser.getId());
        return userMapper.mapToUserDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long userId, UpdateUserRequest request) {
        log.info("Обновление пользователя с id={}", userId);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));


        if (!request.hasName() && !request.hasEmail()) {
            throw new BadRequestException("Не указаны поля для обновления");
        }

        // Проверка уникальности email, если он изменился
        if (request.getEmail() != null &&
                !existingUser.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        User updatedUser = userMapper.updateUserFields(existingUser, request);
        User savedUser = userRepository.save(updatedUser);
        log.info("Пользователь успешно обновлён: id={}", savedUser.getId());
        return userMapper.mapToUserDto(savedUser);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }
}

