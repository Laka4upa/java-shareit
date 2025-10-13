package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void createRequest_WithValidData_ShouldReturnRequestResponse() {
        Long userId = 1L;
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("I need a drill")
                .build();

        User requester = User.builder().id(userId).build();
        ItemRequest request = ItemRequest.builder().id(1L).build();
        ItemRequestResponseDto expectedResponse = ItemRequestResponseDto.builder().id(1L).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(itemRequestMapper.toEntity(requestDto)).thenReturn(request);
        when(itemRequestRepository.save(request)).thenReturn(request);
        when(itemRequestMapper.toDto(request)).thenReturn(expectedResponse);

        ItemRequestResponseDto result = itemRequestService.createRequest(requestDto, userId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(itemRequestRepository).save(request);
    }

    @Test
    void getUserRequests_ShouldReturnUserRequests() {
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        ItemRequest request = ItemRequest.builder().id(1L).build();
        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder().id(1L).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequesterIdOrderByCreatedDesc(userId))
                .thenReturn(List.of(request));
        when(itemRequestMapper.toDto(request)).thenReturn(responseDto);

        List<ItemRequestResponseDto> results = itemRequestService.getUserRequests(userId);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        verify(itemRequestRepository).findByRequesterIdOrderByCreatedDesc(userId);
    }

    @Test
    void getAllRequests_ShouldReturnOtherUsersRequests() {
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        ItemRequest request = ItemRequest.builder().id(1L).build();
        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder().id(1L).build();
        Pageable pageable = Pageable.ofSize(10);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterIdNot(userId, pageable))
                .thenReturn(List.of(request));
        when(itemRequestMapper.toDto(request)).thenReturn(responseDto);

        List<ItemRequestResponseDto> results = itemRequestService.getAllRequests(userId, pageable);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        verify(itemRequestRepository).findAllByRequesterIdNot(userId, pageable);
    }

    @Test
    void getRequestById_WithExistingId_ShouldReturnRequest() {
        Long userId = 1L;
        Long requestId = 1L;
        User user = User.builder().id(userId).build();
        ItemRequest request = ItemRequest.builder().id(requestId).build();
        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder().id(requestId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRequestMapper.toDto(request)).thenReturn(responseDto);

        ItemRequestResponseDto result = itemRequestService.getRequestById(requestId, userId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        verify(itemRequestRepository).findById(requestId);
    }

    @Test
    void getRequestById_WithNonExistingRequest_ShouldThrowException() {
        Long userId = 1L;
        Long requestId = 999L;
        User user = User.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> itemRequestService.getRequestById(requestId, userId));
    }
}