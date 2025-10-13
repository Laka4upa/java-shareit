package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @InjectMocks
    private UserMapper userMapper;

    @Test
    void toDto_WithValidUser_ShouldReturnUserDto() {
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@mail.com")
                .build();

        UserDto dto = userMapper.toDto(user);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("John Doe", dto.getName());
        assertEquals("john@mail.com", dto.getEmail());
    }

    @Test
    void toEntity_WithValidDto_ShouldReturnUser() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@mail.com")
                .build();

        User user = userMapper.toEntity(dto);

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@mail.com", user.getEmail());
    }

    @Test
    void updateEntityFromDto_WithUserUpdateDto_ShouldUpdateOnlyProvidedFields() {
        User existingUser = User.builder()
                .id(1L)
                .name("Original Name")
                .email("original@mail.com")
                .build();

        UserUpdateDto updateDto = UserUpdateDto.builder() // Используем UserUpdateDto
                .name("Updated Name")
                .email(null) // Email не предоставлен
                .build();

        userMapper.updateEntityFromDto(updateDto, existingUser);

        assertEquals("Updated Name", existingUser.getName());
        assertEquals("original@mail.com", existingUser.getEmail()); // Должен остаться неизменным
    }

    @Test
    void updateEntityFromDto_WithUserUpdateDtoAllFields_ShouldUpdateAllFields() {
        User existingUser = User.builder()
                .id(1L)
                .name("Original Name")
                .email("original@mail.com")
                .build();

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("Updated Name")
                .email("updated@mail.com")
                .build();

        userMapper.updateEntityFromDto(updateDto, existingUser);

        assertEquals("Updated Name", existingUser.getName());
        assertEquals("updated@mail.com", existingUser.getEmail());
    }

    @Test
    void updateEntityFromDto_WithUserDto_ShouldUpdateAllFields() {
        User existingUser = User.builder()
                .id(1L)
                .name("Original Name")
                .email("original@mail.com")
                .build();

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("Updated Name")
                .email("updated@mail.com")
                .build();

        userMapper.updateEntityFromDto(updateDto, existingUser);

        assertEquals("Updated Name", existingUser.getName());
        assertEquals("updated@mail.com", existingUser.getEmail());
    }
}