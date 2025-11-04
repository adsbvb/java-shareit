package ru.practicum.shareit.item.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private final List<Item> items;

    @Override
    public Item addItem(Long userId, Item item) {
        item.setId(getId());
        item.setOwner(userId);
        items.add(item);
        return item;
    }

    @Override
    public Item updateItem(Long userId, Long id, Item newItem) throws AccessDeniedException {
        Optional<Item> itemFound = items.stream()
                .filter(i -> i.getId().equals(id) && i.getOwner().equals(userId))
                .findFirst();

        if (itemFound.isPresent()) {
            Item item = itemFound.get();
            if (!userId.equals(item.getOwner())) {
                throw new AccessDeniedException("Access denied!");
            }
            item.setName(newItem.getName());
            item.setDescription(newItem.getDescription());
            item.setAvailable(newItem.getAvailable());
            return item;
        } else {
            throw new NotFoundException("Item", id);
        }
    }

    @Override
    public void deleteItem(Long userId, Long id) {
        boolean removed = items.removeIf(item -> item.getId().equals(id) &&
                item.getOwner().equals(userId));

        if (!removed) {
            throw new NotFoundException("Item", id);
        }
    }

    @Override
    public Item getItem(Long id) {
        return items.stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Item", id));
    }

    @Override
    public List<Item> getItems(Long userId) {
        return items.stream()
                .filter(item -> item.getOwner().equals(userId))
                .toList();
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        String lowerText = text.toLowerCase();

        return items.stream()
                .filter(i -> i.getAvailable() != null && i.getAvailable().equals(true))
                .filter(i -> {
                    String name = i.getName();
                    String description = i.getDescription();
                    return (name != null && name.toLowerCase().contains(lowerText)) ||
                            (description != null && description.toLowerCase().contains(lowerText));
                })
                .toList();
    }

    private long getId() {
        return items.stream()
                .mapToLong(Item::getId)
                .max()
                .orElse(0L) + 1;
    }
}
