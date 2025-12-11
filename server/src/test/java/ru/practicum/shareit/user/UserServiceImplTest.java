package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl unit tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserCreateDto userCreateDto;
    private UserUpdateDto userUpdateDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("User Name")
                .email("test@example.com")
                .build();

        userCreateDto = UserCreateDto.builder()
                .name("User Name")
                .email("test@example.com")
                .build();

        userUpdateDto = UserUpdateDto.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("User Name")
                .email("test@example.com")
                .build();
    }

    @Test
    void createUser_ValidData_ShouldCreateUser() {
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(userMapper.createUserToUser(userCreateDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        UserDto result = userService.createUser(userCreateDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("User Name");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository, times(1)).existsByEmailIgnoreCase("test@example.com");
        verify(userMapper, times(1)).createUserToUser(userCreateDto);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toUserDto(user);
    }

    @Test
    void createUser_ExistingEmail_ShouldThrowIllegalArgumentException() {
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userCreateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists")
                .hasMessageContaining("test@example.com");

        verify(userRepository, times(1)).existsByEmailIgnoreCase("test@example.com");
        verify(userMapper, never()).createUserToUser(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_BothNameAndEmail_ShouldUpdateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCase("updated@example.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        UserDto result = userService.updateUser(1L, userUpdateDto);

        assertThat(result).isNotNull();
        assertThat(user.getName()).isEqualTo("Updated Name");
        assertThat(user.getEmail()).isEqualTo("updated@example.com");

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmailIgnoreCase("updated@example.com");
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toUserDto(user);
    }

    @Test
    void updateUser_OnlyName_ShouldUpdateOnlyName() {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("Updated Name")
                .email(null)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        UserDto result = userService.updateUser(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(user.getName()).isEqualTo("Updated Name");
        assertThat(user.getEmail()).isEqualTo("test@example.com"); // Остался прежним

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).existsByEmailIgnoreCase(anyString()); // Не проверяем email
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUser_OnlyEmail_ShouldUpdateOnlyEmail() {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name(null)
                .email("updated@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCase("updated@example.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        UserDto result = userService.updateUser(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(user.getName()).isEqualTo("User Name"); // Осталось прежним
        assertThat(user.getEmail()).isEqualTo("updated@example.com");

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmailIgnoreCase("updated@example.com");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUser_ExistingNewEmail_ShouldThrowIllegalArgumentException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCase("updated@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, userUpdateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists")
                .hasMessageContaining("updated@example.com");

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmailIgnoreCase("updated@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_NonExistentUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999L, userUpdateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("999");

        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).existsByEmailIgnoreCase(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_ExistingUser_ShouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_NonExistentUser_ShouldThrowNotFoundException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("999");

        verify(userRepository, times(1)).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getUserById_ExistingUser_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        UserDto result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("User Name");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).toUserDto(user);
    }

    @Test
    void getUserById_NonExistentUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("999");

        verify(userRepository, times(1)).findById(999L);
        verify(userMapper, never()).toUserDto(any());
    }

    @Test
    void getAllUser_ShouldReturnAllUsers() {
        List<User> users = List.of(user, User.builder().id(2L).name("User 2").email("user2@example.com").build());
        List<UserDto> userDtos = List.of(
                userDto,
                UserDto.builder().id(2L).name("User 2").email("user2@example.com").build()
        );

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toUserDto(any(User.class)))
                .thenReturn(userDtos.get(0))
                .thenReturn(userDtos.get(1));

        List<UserDto> result = userService.getAllUser();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);

        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(2)).toUserDto(any(User.class));
    }
}