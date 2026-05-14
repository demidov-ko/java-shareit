package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    // ======================== POST /users ========================

    @Test
    void createUser_ValidationData_ShouldReturn201() throws Exception {
        NewUserRequest request = makeRequest("Пользователь", "mail@mail.ru");
        UserDto userDto = makeDto(1L, "Пользователь", "mail@mail.ru");

        when(userService.createUser(any(NewUserRequest.class))).thenReturn(userDto);

        try {
            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(userDto.getId()))
                    .andExpect(jsonPath("$.email").value(userDto.getEmail()))
                    .andExpect(jsonPath("$.name").value(userDto.getName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createUser_NoEmail_ShouldReturn400() throws Exception {
        NewUserRequest request = makeRequest("Пользователь", null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_InvalidEmail_ShouldReturn400() throws Exception {
        NewUserRequest request = makeRequest("Пользователь", "invalid-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_DuplicateEmail_ShouldReturn409() throws Exception {
        NewUserRequest request = makeRequest("Пользователь", "mail@mail.ru");

        when(userService.createUser(any(NewUserRequest.class)))
                .thenThrow(new ConflictException("Email уже существует"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    // ======================== GET /users ========================

    @Test
    void getAllUsers_ShouldReturn200() throws Exception {
        User user = makeUser(1L, "Пользователь", "mail@mail.ru");
        UserDto userDto = makeDto(1L, "Пользователь", "mail@mail.ru");

        when(userService.findAll()).thenReturn(List.of(user));
        when(userMapper.mapToUserDto(user)).thenReturn(userDto);

        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("mail@mail.ru"))
                .andExpect(jsonPath("$[0].name").value("Пользователь"));
    }

    // ======================== GET /users/{id} ========================

    @Test
    void getUserById_Exists_ShouldReturn200() throws Exception {
        User user = makeUser(1L, "Пользователь", "mail@mail.ru");
        UserDto userDto = makeDto(1L, "Пользователь", "mail@mail.ru");

        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.mapToUserDto(user)).thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("mail@mail.ru"))
                .andExpect(jsonPath("$.name").value("Пользователь"));
    }

    @Test
    void getUserById_NotFound_ShouldReturn404() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound());
    }

    // ======================== PATCH /users/{id} ========================

    @Test
    void updateUser_ValidData_ShouldReturn200() throws Exception {
        UpdateUserRequest request = makeUpdateRequest("Новое имя", "new@mail.ru");
        UserDto userDto = makeDto(1L, "Новое имя", "new@mail.ru");

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(userDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("new@mail.ru"))
                .andExpect(jsonPath("$.name").value("Новое имя"));
    }

    @Test
    void updateUser_DuplicateEmail_ShouldReturn409() throws Exception {
        UpdateUserRequest request = makeUpdateRequest(null, "existing@mail.ru");

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
                .thenThrow(new ConflictException("Email уже существует"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_NotFound_ShouldReturn404() throws Exception {
        UpdateUserRequest request = makeUpdateRequest("Имя", "mail@mail.ru");

        when(userService.updateUser(eq(99L), any(UpdateUserRequest.class)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(patch("/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }


    // ======================== helpers ========================

    private NewUserRequest makeRequest(String name, String email) {
        NewUserRequest r = new NewUserRequest();
        r.setName(name);
        r.setEmail(email);
        return r;
    }

    private UpdateUserRequest makeUpdateRequest(String name, String email) {
        UpdateUserRequest r = new UpdateUserRequest();
        r.setName(name);
        r.setEmail(email);
        return r;
    }

    private UserDto makeDto(Long id, String name, String email) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    private User makeUser(Long id, String name, String email) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }
}
