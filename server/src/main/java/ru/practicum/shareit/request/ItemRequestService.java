package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestResponseDto createRequest(ItemRequestDto requestDto, Long userId);

    List<ItemRequestResponseDto> getUserRequests(Long userId);

    List<ItemRequestResponseDto> getAllRequests(Long userId, Pageable pageable);

    List<ItemRequestResponseDto> getAllRequests(Long userId);

    ItemRequestResponseDto getRequestById(Long requestId, Long userId);
}