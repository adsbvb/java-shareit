package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dal.CommentRepository;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndComments;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ItemServiceImpl.getItemForOwner() integration tests")
public class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User owner;
    private User booker1;
    private User booker2;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        booker1 = userRepository.save(User.builder()
                .name("Booker 1")
                .email("booker1@example.com")
                .build());

        booker2 = userRepository.save(User.builder()
                .name("Booker 2")
                .email("booker2@example.com")
                .build());

        item1 = itemRepository.save(Item.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .owner(owner)
                .build());

        item2 = itemRepository.save(Item.builder()
                .name("Перфоратор")
                .description("Мощный перфоратор")
                .available(true)
                .owner(owner)
                .build());
    }

    @Test
    void getItemForOwner_whenItemsHaveBookingsAndComments_shouldReturnAllData() {
        LocalDateTime now = LocalDateTime.now();

        Booking pastBooking = bookingRepository.save(Booking.builder()
                .start(now.minusDays(10))
                .end(now.minusDays(8))
                .item(item1)
                .booker(booker1)
                .status(Status.APPROVED)
                .build());

        Booking futureBooking = bookingRepository.save(Booking.builder()
                .start(now.plusDays(2))
                .end(now.plusDays(4))
                .item(item1)
                .booker(booker2)
                .status(Status.APPROVED)
                .build());

        commentRepository.save(Comment.builder()
                .text("Пользовался раньше, хорошая вещь")
                .item(item1)
                .author(booker1)
                .created(now.minusDays(5))
                .build());

        bookingRepository.save(Booking.builder()
                .start(now.minusDays(3))
                .end(now.minusDays(1))
                .item(item2)
                .booker(booker1)
                .status(Status.APPROVED)
                .build());

        commentRepository.save(Comment.builder()
                .text("Взял на выходные, все понравилось")
                .item(item2)
                .author(booker1)
                .created(now.minusHours(12))
                .build());

        List<ItemWithBookingsAndComments> result = itemService.getItemForOwner(owner.getId());

        assertThat(result).hasSize(2);

        ItemWithBookingsAndComments item1Dto = result.stream()
                .filter(dto -> dto.getId().equals(item1.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(item1Dto.getLastBooking()).isNotNull();
        assertThat(item1Dto.getLastBooking().getId()).isEqualTo(pastBooking.getId());
        assertThat(item1Dto.getNextBooking()).isNotNull();
        assertThat(item1Dto.getNextBooking().getId()).isEqualTo(futureBooking.getId());
        assertThat(item1Dto.getComments()).hasSize(1);
        assertThat(item1Dto.getComments().get(0).getText())
                .isEqualTo("Пользовался раньше, хорошая вещь");

        ItemWithBookingsAndComments item2Dto = result.stream()
                .filter(dto -> dto.getId().equals(item2.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(item2Dto.getLastBooking()).isNotNull();
        assertThat(item2Dto.getNextBooking()).isNull();
        assertThat(item2Dto.getComments()).hasSize(1);
    }
}