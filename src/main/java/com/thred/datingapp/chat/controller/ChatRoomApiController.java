package com.thred.datingapp.chat.controller;

import com.thred.datingapp.chat.dto.response.ChatRoomAllResponse;
import com.thred.datingapp.chat.dto.response.ChatRoomResponse;
import com.thred.datingapp.chat.service.ChatRoomService;
import com.thred.datingapp.chat.service.SseEmitterService;
import com.thred.datingapp.common.api.response.ApiDataResponse;
import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.user.argumentResolver.Login;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequestMapping("/api/chat/room")
@RequiredArgsConstructor
@RestController
public class ChatRoomApiController {
    private final ChatRoomService   chatRoomService;
    private final SseEmitterService sseEmitterService;

    @PostMapping("/create/{receiverId}")
    public ApiDataResponse<ChatRoomResponse> createChatRoom(
            @Login Long userId,
            @PathVariable("receiverId") Long receiverId) {
        log.debug("[createChatRoom] userId: {}", userId);
        log.debug("[createChatRoom] receiverId: {}", receiverId);
        return ApiDataResponse.ok(chatRoomService.createChatRoom(userId, receiverId));
    }

    @GetMapping("/all")
    public ApiDataResponse<PageResponse<ChatRoomAllResponse>> getAllChatRooms(
        @Login Long userId,
        @RequestParam("pageLastId") Long pageLastId,
        @RequestParam("pageSize")int pageSize) {
        log.debug("[getAllChatRooms] userId: {}", userId);
        log.debug("[getAllChatRooms] pageLastId: {}", pageLastId);
        log.debug("[getAllChatRooms] pageSize: {}", pageSize);
        return ApiDataResponse.ok(chatRoomService.getAllChatRooms(userId, pageLastId, pageSize));
    }

    @DeleteMapping("/delete/{chatRoomId}")
    public ApiDataResponse<Boolean> deleteChatRoom(@PathVariable(name = "chatRoomId") Long chatRoomId) {
        log.debug("[deleteChatRoom] chatRoomId: {}", chatRoomId);
        return ApiDataResponse.ok(chatRoomService.deleteChatRoom(chatRoomId));
    }

    @GetMapping(value = "/sse/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ApiDataResponse<SseEmitter> connectSse(@Login Long userId) {
        log.debug("[connectSse] userId: {}", userId);
        return ApiDataResponse.ok(sseEmitterService.createEmitter(userId));
    }

    @GetMapping(value = "/sse/disconnect")
    public ApiDataResponse<Boolean> disconnectSse(@Login Long userId) {
        log.debug("[disconnectSse] userId: {}", userId);
        return ApiDataResponse.ok(sseEmitterService.disconnect(userId));
    }
}
