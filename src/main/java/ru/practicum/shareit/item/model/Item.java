package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            //Уникальный идентификатор вещи

    @Column(nullable = false)
    private String name;        //краткое название

    @Column(nullable = false)
    private String description; //развёрнутое описание

    @Column(nullable = false)
    private Boolean available;  //статус о том, доступна или нет вещь для аренды;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;         //владелец вещи

//    @ManyToOne
//    @JoinColumn(name = "request_id", nullable = false)
//    private ItemRequest request; //если вещь была создана по запросу другого пользователя, то в этом
//    //поле будет храниться ссылка на соответствующий запрос.
}
