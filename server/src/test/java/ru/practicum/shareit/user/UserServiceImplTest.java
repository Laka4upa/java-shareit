package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_WithValidData_ShouldReturnUserDto() {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("john@mail.com")
                .build();

        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@mail.com")
                .build();

        UserDto expectedDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@mail.com")
                .build();

        when(userMapper.toEntity(userDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(userRepository).save(user);
    }

    @Test
    void getUserById_WithExistingId_ShouldReturnUserDto() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("John Doe")
                .email("john@mail.com")
                .build();

        UserDto expectedDto = UserDto.builder()
                .id(userId)
                .name("John Doe")
                .email("john@mail.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        UserDto result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userRepository).findById(userId);
    }

    @Test
    void updateUser_WithUserUpdateDto_ShouldReturnUpdatedUser() {
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .name("Original Name")
                .email("original@mail.com")
                .build();

        UserUpdateDto updateDto = UserUpdateDto.builder() // Используем UserUpdateDto
                .name("Updated Name")
                .email("updated@mail.com")
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .name("Updated Name")
                .email("updated@mail.com")
                .build();

        UserDto expectedDto = UserDto.builder()
                .id(userId)
                .name("Updated Name")
                .email("updated@mail.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailAndIdNot("updated@mail.com", userId)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(expectedDto);

        UserDto result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@mail.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WithUserUpdateDtoPartialData_ShouldUpdateOnlyProvidedFields() {
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .name("Original Name")
                .email("original@mail.com")
                .build();

        UserUpdateDto updateDto = UserUpdateDto.builder() // Только имя
                .name("Updated Name")
                .email(null)
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .name("Updated Name")
                .email("original@mail.com")
                .build();

        UserDto expectedDto = UserDto.builder()
                .id(userId)
                .name("Updated Name")
                .email("original@mail.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(expectedDto);

        UserDto result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("original@mail.com", result.getEmail()); // Email должен остаться прежним
    }

    @Test
    void updateUser_WithUserDto_ShouldReturnUpdatedUser() {
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .name("Original Name")
                .email("original@mail.com")
                .build();

        UserUpdateDto updateDto = UserUpdateDto.builder() // Используем UserDto
                .name("Updated Name")
                .email("updated@mail.com")
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .name("Updated Name")
                .email("updated@mail.com")
                .build();

        UserDto expectedDto = UserDto.builder()
                .id(userId)
                .name("Updated Name")
                .email("updated@mail.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailAndIdNot("updated@mail.com", userId)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(expectedDto);

        UserDto result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@mail.com", result.getEmail());
    }

    @Test
    void updateUser_WithDuplicateEmail_ShouldThrowConflictException() {
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .name("Original Name")
                .email("original@mail.com")
                .build();

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("duplicate@mail.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailAndIdNot("duplicate@mail.com", userId)).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.updateUser(userId, updateDto));
    }

    @Test
    void deleteUser_WithExistingId_ShouldDeleteUser() {
        Long userId = 1L;

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }
}