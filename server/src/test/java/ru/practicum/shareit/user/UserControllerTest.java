package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("John Doe")
            .email("john@example.com")
            .build();

    private final UserUpdateDto userUpdateDto = UserUpdateDto.builder()
            .name("Updated Name")
            .email("updated@example.com")
            .build();

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService).createUser(any(UserDto.class));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        List<UserDto> users = List.of(
                UserDto.builder().id(1L).name("User 1").email("user1@example.com").build(),
                UserDto.builder().id(2L).name("User 2").email("user2@example.com").build()
        );

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("User 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("User 2"));

        verify(userService).getAllUsers();
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        when(userService.updateUser(eq(1L), any(UserUpdateDto.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService).updateUser(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    void deleteUser_ShouldCallService() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isOk());

        verify(userService).deleteUser(1L);
    }

    @Test
    void updateUser_WithOnlyName_ShouldWork() throws Exception {
        UserUpdateDto partialUpdate = UserUpdateDto.builder()
                .name("Only Name Updated")
                .email(null)
                .build();

        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .name("Only Name Updated")
                .email("john@example.com")
                .build();

        when(userService.updateUser(eq(1L), any(UserUpdateDto.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Only Name Updated"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void updateUser_WithOnlyEmail_ShouldWork() throws Exception {
        UserUpdateDto partialUpdate = UserUpdateDto.builder()
                .name(null)
                .email("only-email@example.com")
                .build();

        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("only-email@example.com")
                .build();

        when(userService.updateUser(eq(1L), any(UserUpdateDto.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("only-email@example.com"));
    }
}