package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //— уникальный идентификатор комментария;

    @Column(nullable = false)
    private String text; //— содержимое комментария;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item; //— вещь, к которой относится комментарий;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author; //— автор комментария;

    @Column(nullable = false)
    private LocalDateTime created; //— дата создания комментария.
}
