package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> tester;

    @Test
    void commentDto_Serialize_ShouldContainAllFields() throws Exception {
        CommentDto dto = new CommentDto();
        dto.setId(1L);
        dto.setText("Отличная дрель!");
        dto.setAuthorName("Иван");
        dto.setCreated(LocalDateTime.of(2026, 6, 20, 10, 0, 0));

        var json = tester.write(dto);

        assertThat(json).hasJsonPath("$.id");
        assertThat(json).hasJsonPath("$.text");
        assertThat(json).hasJsonPath("$.authorName");
        assertThat(json).hasJsonPath("$.created");
        assertThat(json).extractingJsonPathStringValue("$.text")
                .isEqualTo("Отличная дрель!");
        assertThat(json).extractingJsonPathStringValue("$.authorName")
                .isEqualTo("Иван");
    }

    @Test
    void commentDto_Deserialize_ShouldParseCorrectly() throws Exception {
        // CHECKSTYLE:OFF
        String json = """
                {
                "id": 1,
                "text": "Отличная дрель!",
                "authorName": "Иван",
                "created": "2026-06-20T10:00:00"
                }
                """;
        // CHECKSTYLE:ON

        CommentDto dto = tester.parseObject(json);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Отличная дрель!");
        assertThat(dto.getAuthorName()).isEqualTo("Иван");
        assertThat(dto.getCreated())
                .isEqualTo(LocalDateTime.of(2026, 6, 20, 10, 0, 0));
    }
}