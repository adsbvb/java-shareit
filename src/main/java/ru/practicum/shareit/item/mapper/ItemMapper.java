package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndComments;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "name", source = "dto.name")
    Item toItem(ItemCreateDto dto, User owner);

    ItemResponseDto toItemResponseDto(Item item);

    @Mapping(target = "lastBooking", ignore = true)
    @Mapping(target = "nextBooking",  ignore = true)
    @Mapping(target = "comments", ignore = true)
    ItemWithBookingsAndComments toItemWithBookingsAndComments(Item item);
}
