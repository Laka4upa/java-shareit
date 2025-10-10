package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    @Transactional
    public ItemRequestResponseDto createRequest(ItemRequestDto requestDto, Long userId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден с id: " + userId));

        ItemRequest itemRequest = itemRequestMapper.toEntity(requestDto);
        itemRequest.setRequester(requester);

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        return itemRequestMapper.toDto(savedRequest);
    }

    @Override
    public List<ItemRequestResponseDto> getUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден с id: " + userId));

        return itemRequestRepository.findByRequesterIdOrderByCreatedDesc(userId).stream()
                .map(itemRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден с id: " + userId));

        return itemRequestRepository.findAllByRequesterIdNot(userId, pageable).stream()
                .map(itemRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден с id: " + userId));

        return itemRequestRepository.findAllByRequesterIdNot(userId).stream()
                .map(itemRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long requestId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден с id: " + userId));

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Запрос не найден с id: " + requestId));

        return itemRequestMapper.toDto(itemRequest);
    }
}