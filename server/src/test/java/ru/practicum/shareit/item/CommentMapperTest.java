package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class CommentMapperTest {

    @InjectMocks
    private CommentMapper commentMapper;

    @Test
    void toEntity_WithValidDto_ShouldReturnComment() {
        CommentRequestDto dto = CommentRequestDto.builder()
                .text("Great item!")
                .build();

        Comment comment = commentMapper.toEntity(dto);

        assertNotNull(comment);
        assertEquals("Great item!", comment.getText());
        assertNotNull(comment.getCreated());
    }

    @Test
    void toDto_WithValidComment_ShouldReturnResponseDto() {
        User author = User.builder()
                .id(1L)
                .name("Booker")
                .email("booker@mail.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .text("Great item!")
                .item(item)
                .author(author)
                .created(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();

        CommentResponseDto dto = commentMapper.toDto(comment);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Great item!", dto.getText());
        assertEquals("Booker", dto.getAuthorName());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), dto.getCreated());
    }
}