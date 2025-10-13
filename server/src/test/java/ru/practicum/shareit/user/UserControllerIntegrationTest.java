package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void createUser_WithValidData_ShouldCreateUser() {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("john@mail.com")
                .build();

        UserDto result = userService.createUser(userDto);

        assertNotNull(result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@mail.com", result.getEmail());
    }

    @Test
    void getUserById_WithExistingId_ShouldReturnUser() {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("john@mail.com")
                .build();
        UserDto savedUser = userService.createUser(userDto);

        UserDto result = userService.getUserById(savedUser.getId());

        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@mail.com", result.getEmail());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        userService.createUser(UserDto.builder()
                .name("User 1")
                .email("user1@mail.com")
                .build());

        userService.createUser(UserDto.builder()
                .name("User 2")
                .email("user2@mail.com")
                .build());

        List<UserDto> results = userService.getAllUsers();

        assertFalse(results.isEmpty());
        assertTrue(results.size() >= 2);
        assertTrue(results.stream().anyMatch(user -> user.getName().equals("User 1")));
        assertTrue(results.stream().anyMatch(user -> user.getName().equals("User 2")));
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        UserDto originalUser = userService.createUser(UserDto.builder()
                .name("Original Name")
                .email("original@mail.com")
                .build());

        UserUpdateDto updateDto = UserUpdateDto.builder() // Используем UserUpdateDto
                .name("Updated Name")
                .email("updated@mail.com")
                .build();

        UserDto result = userService.updateUser(originalUser.getId(), updateDto);

        assertEquals(originalUser.getId(), result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@mail.com", result.getEmail());
    }

    @Test
    void updateUser_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        UserDto originalUser = userService.createUser(UserDto.builder()
                .name("Original Name")
                .email("original@mail.com")
                .build());

        UserUpdateDto updateDto = UserUpdateDto.builder() // Только имя, email остается прежним
                .name("Updated Name")
                .email(null)
                .build();

        UserDto result = userService.updateUser(originalUser.getId(), updateDto);

        assertEquals(originalUser.getId(), result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("original@mail.com", result.getEmail()); // Email должен остаться прежним
    }

    @Test
    void deleteUser_WithExistingId_ShouldDeleteUser() {
        UserDto user = userService.createUser(UserDto.builder()
                .name("To Delete")
                .email("delete@mail.com")
                .build());

        userService.deleteUser(user.getId());

        assertThrows(Exception.class, () -> userService.getUserById(user.getId()));
    }
}