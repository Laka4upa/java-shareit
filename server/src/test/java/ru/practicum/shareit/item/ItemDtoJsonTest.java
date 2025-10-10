package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemCreateDto> itemCreateJson;

    @Autowired
    private JacksonTester<ItemDto> itemJson;

    @Autowired
    private JacksonTester<CommentRequestDto> commentRequestJson;

    @Autowired
    private JacksonTester<CommentResponseDto> commentResponseJson;

    @Test
    void itemCreateDto_ShouldSerializeCorrectly() throws Exception {
        ItemCreateDto dto = ItemCreateDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .requestId(10L)
                .build();

        var jsonContent = itemCreateJson.write(dto);

        assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo("Item");
        assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo("Description");
        assertThat(jsonContent).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(jsonContent).extractingJsonPathNumberValue("$.requestId").isEqualTo(10);
    }

    @Test
    void itemDto_ShouldSerializeWithAllFields() throws Exception {
        BookingShortDto lastBooking = BookingShortDto.builder()
                .id(1L)
                .bookerId(2L)
                .start(LocalDateTime.of(2024, 1, 1, 10, 0))
                .end(LocalDateTime.of(2024, 1, 2, 10, 0))
                .build();

        BookingShortDto nextBooking = BookingShortDto.builder()
                .id(2L)
                .bookerId(3L)
                .start(LocalDateTime.of(2024, 1, 3, 10, 0))
                .end(LocalDateTime.of(2024, 1, 4, 10, 0))
                .build();

        CommentResponseDto comment = CommentResponseDto.builder()
                .id(1L)
                .text("Great!")
                .authorName("User")
                .created(LocalDateTime.of(2024, 1, 1, 12, 0))
                .build();

        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .ownerId(1L)
                .requestId(10L)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of(comment))
                .build();

        var jsonContent = itemJson.write(dto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo("Item");
        assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo("Description");
        assertThat(jsonContent).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(jsonContent).extractingJsonPathNumberValue("$.ownerId").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.requestId").isEqualTo(10);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(2);
        assertThat(jsonContent).extractingJsonPathArrayValue("$.comments").hasSize(1);
    }

    @Test
    void commentRequestDto_ShouldSerializeCorrectly() throws Exception {
        CommentRequestDto dto = CommentRequestDto.builder()
                .text("Great item!")
                .build();

        var jsonContent = commentRequestJson.write(dto);

        assertThat(jsonContent).extractingJsonPathStringValue("$.text").isEqualTo("Great item!");
    }

    @Test
    void commentResponseDto_ShouldSerializeCorrectly() throws Exception {
        CommentResponseDto dto = CommentResponseDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("Booker")
                .created(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();

        var jsonContent = commentResponseJson.write(dto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.text").isEqualTo("Great item!");
        assertThat(jsonContent).extractingJsonPathStringValue("$.authorName").isEqualTo("Booker");
        assertThat(jsonContent).extractingJsonPathStringValue("$.created")
                .isEqualTo("2024-01-01T10:00:00");
    }

    @Test
    void itemCreateDto_ShouldDeserializeCorrectly() throws Exception {
        String content = """
                {
                    "name": "Item",
                    "description": "Description",
                    "available": true,
                    "requestId": 10
                }
                """;

        ItemCreateDto dto = itemCreateJson.parseObject(content);

        assertThat(dto.getName()).isEqualTo("Item");
        assertThat(dto.getDescription()).isEqualTo("Description");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(10L);
    }
}