package ru.practicum.shareit.request;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "requests")
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //уникальный идентификатор запроса;

    @Column(nullable = false)
    private String description; //текст запроса, содержащий описание требуемой вещи;

    @ManyToOne
    @JoinColumn(name = "requestor_id", nullable = false)
    private User requestor; //пользователь, создавший запрос;

    private LocalDateTime created; //дата и время создания запроса.

}
