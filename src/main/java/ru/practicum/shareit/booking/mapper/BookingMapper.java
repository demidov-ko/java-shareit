package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.mapper.UserMapper;

//зависимсоть ItemMapper и UserMapper, т.к. Mapstruct увидит разные типы и не сможет смаппить автоматически
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ItemMapper.class, UserMapper.class})
public interface BookingMapper {
    BookingDto mapToBookingDto(Booking booking);
}
