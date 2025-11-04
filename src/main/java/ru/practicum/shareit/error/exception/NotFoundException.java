package ru.practicum.shareit.error.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotFoundException extends RuntimeException {
    private final String entityType;
    private final Long id;

    public NotFoundException(String entityType, Long id) {
        super(entityType + " with id " + id + " not found");
        this.entityType = entityType;
        this.id = id;
    }
}