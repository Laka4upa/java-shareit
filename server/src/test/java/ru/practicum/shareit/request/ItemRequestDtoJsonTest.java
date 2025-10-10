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
                .description("Мне нужна дрель")
                .build();

        var jsonContent = requestJson.write(dto);

        assertThat(jsonContent).extractingJsonPathStringValue("$.description")
                .isEqualTo("Мне нужна дрель");
    }

    @Test
    void itemRequestResponseDto_ShouldSerializeWithAllFields() throws Exception {
        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();

        ItemRequestResponseDto dto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Мне нужна дрель")
                .created(LocalDateTime.of(2024, 1, 1, 10, 0))
                .items(List.of(item))
                .build();

        var jsonContent = responseJson.write(dto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.description")
                .isEqualTo("Мне нужна дрель");
        assertThat(jsonContent).extractingJsonPathStringValue("$.created")
                .isEqualTo("2024-01-01T10:00:00");
        assertThat(jsonContent).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.items[0].name")
                .isEqualTo("Дрель");
    }

    @Test
    void itemRequestDto_ShouldDeserializeCorrectly() throws Exception {
        String content = "{\n" +
                "    \"description\": \"Мне нужна дрель\"\n" +
                "}";

        ItemRequestDto dto = requestJson.parseObject(content);

        assertThat(dto.getDescription()).isEqualTo("Мне нужна дрель");
    }

    @Test
    void itemRequestResponseDto_ShouldDeserializeCorrectly() throws Exception {
        String content = "{\n" +
                "    \"id\": 1,\n" +
                "    \"description\": \"Мне нужна дрель\",\n" +
                "    \"created\": \"2024-01-01T10:00:00\",\n" +
                "    \"items\": [\n" +
                "        {\n" +
                "            \"id\": 1,\n" +
                "            \"name\": \"Дрель\",\n" +
                "            \"description\": \"Мощная дрель\",\n" +
                "            \"available\": true\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        ItemRequestResponseDto dto = responseJson.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Мне нужна дрель");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Дрель");
    }
}