package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingRequestDto> requestJson;

    @Autowired
    private JacksonTester<BookingResponseDto> responseJson;

    @Test
    void bookingRequestDto_ShouldSerializeCorrectly() throws Exception {
        BookingRequestDto dto = BookingRequestDto.builder().itemId(1L).start(LocalDateTime.of(2024, 1, 1, 10, 0)).end(LocalDateTime.of(2024, 1, 2, 10, 0)).build();

        var jsonContent = requestJson.write(dto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.start").isEqualTo("2024-01-01T10:00:00");
        assertThat(jsonContent).extractingJsonPathStringValue("$.end").isEqualTo("2024-01-02T10:00:00");
    }

    @Test
    void bookingRequestDto_ShouldDeserializeCorrectly() throws Exception {
        String content = "{\n" +
                "    \"itemId\": 1,\n" +
                "    \"start\": \"2024-01-01T10:00:00\",\n" +
                "    \"end\": \"2024-01-02T10:00:00\"\n" +
                "}";

        BookingRequestDto dto = requestJson.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 2, 10, 0));
    }

    @Test
    void bookingResponseDto_ShouldSerializeCorrectly() throws Exception {
        UserDto booker = UserDto.builder().id(1L).name("Booker").build();
        ItemDto item = ItemDto.builder().id(1L).name("Item").build();

        BookingResponseDto dto = BookingResponseDto.builder().id(1L).start(LocalDateTime.of(2024, 1, 1, 10, 0)).end(LocalDateTime.of(2024, 1, 2, 10, 0)).status(BookingStatus.APPROVED).booker(booker).item(item).build();

        var jsonContent = responseJson.write(dto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.start").isEqualTo("2024-01-01T10:00:00");
        assertThat(jsonContent).extractingJsonPathStringValue("$.end").isEqualTo("2024-01-02T10:00:00");
        assertThat(jsonContent).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
    }
}