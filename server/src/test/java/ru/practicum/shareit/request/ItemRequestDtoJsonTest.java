package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> requestJson;

    @Autowired
    private JacksonTester<ItemRequestResponseDto> responseJson;

    @Test
    void itemRequestDto_ShouldSerializeCorrectly() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("I need a drill")
                .build();

        var jsonContent = requestJson.write(dto);

        assertThat(jsonContent).extractingJsonPathStringValue("$.description")
                .isEqualTo("I need a drill");
    }

    @Test
    void itemRequestResponseDto_ShouldSerializeWithAllFields() throws Exception {
        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        ItemRequestResponseDto dto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("I need a drill")
                .created(LocalDateTime.of(2024, 1, 1, 10, 0))
                .items(List.of(item))
                .build();

        var jsonContent = responseJson.write(dto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.description")
                .isEqualTo("I need a drill");
        assertThat(jsonContent).extractingJsonPathStringValue("$.created")
                .isEqualTo("2024-01-01T10:00:00");
        assertThat(jsonContent).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.items[0].name")
                .isEqualTo("Drill");
    }

    @Test
    void itemRequestDto_ShouldDeserializeCorrectly() throws Exception {
        String content = """
                {
                    "description": "I need a drill"
                }
                """;

        ItemRequestDto dto = requestJson.parseObject(content);

        assertThat(dto.getDescription()).isEqualTo("I need a drill");
    }

    @Test
    void itemRequestResponseDto_ShouldDeserializeCorrectly() throws Exception {
        String content = """
                {
                    "id": 1,
                    "description": "I need a drill",
                    "created": "2024-01-01T10:00:00",
                    "items": [
                        {
                            "id": 1,
                            "name": "Drill",
                            "description": "Powerful drill",
                            "available": true
                        }
                    ]
                }
                """;

        // When
        ItemRequestResponseDto dto = responseJson.parseObject(content);

        // Then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("I need a drill");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Drill");
    }
}