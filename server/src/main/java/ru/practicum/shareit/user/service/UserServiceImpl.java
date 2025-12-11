package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(UserCreateDto userCreateDto) {
        if (userRepository.existsByEmailIgnoreCase(userCreateDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists! - " + userCreateDto.getEmail());
        }
        User user  = userMapper.createUserToUser(userCreateDto);
        userRepository.save(user);
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userid, UserUpdateDto userUpdateDto) {
        User existingUser = userRepository.findById(userid)
                .orElseThrow(() -> new NotFoundException("User", userid));

        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().trim().isEmpty()) {

            String existingEmail = existingUser.getEmail().trim().toLowerCase();
            String newEmail = userUpdateDto.getEmail().trim().toLowerCase();

            if (!existingEmail.equals(newEmail)) {
                if (userRepository.existsByEmailIgnoreCase(newEmail)) {
                    throw new IllegalArgumentException("Email already exists! - " + userUpdateDto.getEmail());
                }
            }

            //existingUser.setEmail(newEmail);
            existingUser.setEmail(userUpdateDto.getEmail().trim());
        }

        if (userUpdateDto.getName() != null && !userUpdateDto.getName().trim().isEmpty()) {
            existingUser.setName(userUpdateDto.getName());
        }

        userRepository.save(existingUser);
        return userMapper.toUserDto(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
        return userMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUser() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toUserDto)
                .toList();
    }
}
