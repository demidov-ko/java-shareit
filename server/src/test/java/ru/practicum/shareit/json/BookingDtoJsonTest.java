package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> bookingDtoTester;

    @Autowired
    private JacksonTester<NewBookingRequest> newBookingRequestTester;

    @Test
    void bookingDto_SerializeDateTime_ShouldFormatCorrectly() throws Exception {
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStatus(Status.WAITING);
        dto.setStart(LocalDateTime.of(2026, 6, 20, 10, 0, 0));
        dto.setEnd(LocalDateTime.of(2026, 6, 21, 10, 0, 0));

        var json = bookingDtoTester.write(dto);

        assertThat(json).hasJsonPath("$.id");
        assertThat(json).hasJsonPath("$.status");
        assertThat(json).hasJsonPath("$.start");
        assertThat(json).hasJsonPath("$.end");
        assertThat(json).extractingJsonPathStringValue("$.status")
                .isEqualTo("WAITING");
    }

    @Test
    void bookingDto_DeserializeDateTime_ShouldParseCorrectly() throws Exception {
        // CHECKSTYLE:OFF
        String json = """
                {
                "id": 1,
                "status": "APPROVED",
                "start": "2026-06-20T10:00:00",
                "end": "2026-06-21T10:00:00"
                }
                """;
        // CHECKSTYLE:ON

        BookingDto dto = bookingDtoTester.parseObject(json);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 6, 20, 10, 0, 0));
    }

    @Test
    void newBookingRequest_Serialize_ShouldContainAllFields() throws Exception {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.of(2026, 6, 20, 10, 0, 0));
        request.setEnd(LocalDateTime.of(2026, 6, 21, 10, 0, 0));

        var json = newBookingRequestTester.write(request);

        assertThat(json).hasJsonPath("$.itemId");
        assertThat(json).hasJsonPath("$.start");
        assertThat(json).hasJsonPath("$.end");
        assertThat(json).extractingJsonPathNumberValue("$.itemId")
                .isEqualTo(1);
    }

    @Test
    void newBookingRequest_Deserialize_ShouldParseCorrectly() throws Exception {
        // CHECKSTYLE:OFF
        String json = """
                {
                "itemId": 1,
                "start": "2026-06-20T10:00:00",
                "end": "2026-06-21T10:00:00"
                }
                """;
        // CHECKSTYLE:ON

        NewBookingRequest request = newBookingRequestTester.parseObject(json);

        assertThat(request.getItemId()).isEqualTo(1L);
        assertThat(request.getStart())
                .isEqualTo(LocalDateTime.of(2026, 6, 20, 10, 0, 0));
        assertThat(request.getEnd())
                .isEqualTo(LocalDateTime.of(2026, 6, 21, 10, 0, 0));
    }
}
