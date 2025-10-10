package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    public static final String HEADER = "X-Sharer-User-Id";
    private final ItemRequestClient itemRequestClient;

    @GetMapping
    public ResponseEntity<Object> getRequests(@RequestHeader(HEADER) Long userId) {
        return itemRequestClient.getUserRequests(userId);
    }

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(HEADER) Long userId,
                                                @RequestBody @Valid ItemRequestDto requestDto) {
        return itemRequestClient.createRequest(userId, requestDto);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(HEADER) Long userId,
                                                 @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                 @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemRequestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequest(@RequestHeader(HEADER) Long userId,
                                             @PathVariable Long requestId) {
        return itemRequestClient.getRequestById(userId, requestId);
    }
}