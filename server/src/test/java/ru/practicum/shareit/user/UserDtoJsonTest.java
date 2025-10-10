package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> userDtoJson;

    @Autowired
    private JacksonTester<UserUpdateDto> userUpdateDtoJson;

    @Test
    void userDto_ShouldSerializeCorrectly() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@mail.com")
                .build();

        var jsonContent = userDtoJson.write(dto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo("John Doe");
        assertThat(jsonContent).extractingJsonPathStringValue("$.email").isEqualTo("john@mail.com");
    }

    @Test
    void userDto_ShouldDeserializeCorrectly() throws Exception {
        String content = """
                {
                    "id": 1,
                    "name": "John Doe",
                    "email": "john@mail.com"
                }
                """;

        UserDto dto = userDtoJson.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getEmail()).isEqualTo("john@mail.com");
    }

    @Test
    void userUpdateDto_ShouldSerializeCorrectly() throws Exception {
        UserUpdateDto dto = UserUpdateDto.builder()
                .name("John Doe")
                .email("john@mail.com")
                .build();

        var jsonContent = userUpdateDtoJson.write(dto);

        assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo("John Doe");
        assertThat(jsonContent).extractingJsonPathStringValue("$.email").isEqualTo("john@mail.com");
    }

    @Test
    void userUpdateDto_ShouldDeserializeCorrectly() throws Exception {
        String content = """
                {
                    "name": "John Doe",
                    "email": "john@mail.com"
                }
                """;

        UserUpdateDto dto = userUpdateDtoJson.parseObject(content);

        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getEmail()).isEqualTo("john@mail.com");
    }

    @Test
    void userUpdateDto_ShouldDeserializeWithPartialData() throws Exception {
        String content = """
                {
                    "name": "John Doe"
                }
                """;

        UserUpdateDto dto = userUpdateDtoJson.parseObject(content);

        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getEmail()).isNull();
    }
}