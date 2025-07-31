package com.thred.datingapp.chat.controller;

import com.thred.datingapp.chat.dto.ChatMessageResponse;
import com.thred.datingapp.chat.dto.request.ChatMessageRequest;
import com.thred.datingapp.chat.dto.response.ChatResponse;
import com.thred.datingapp.chat.service.ChatService;
import com.thred.datingapp.common.api.response.ApiDataResponse;
import com.thred.datingapp.common.api.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@RestController
public class ChatApiController {

    private final ChatService chatService;

    @GetMapping("/all/{chatRoomId}")
    public ApiDataResponse<PageResponse<ChatResponse>> getChatMessagesWithPaging(
        @PathVariable("chatRoomId") Long chatRoomId,
        @RequestParam("pageLastId") Long pageLastId,
        @RequestParam("pageSize") int pageSize
        ){
        log.debug("[getChatMessagesWithPaging] chatRoomId: {}", chatRoomId);
        log.debug("[getChatMessagesWithPaging] pageLastId: {}", pageLastId);
        log.debug("[getChatMessagesWithPaging] pageSize: {}", pageSize);
        return ApiDataResponse.ok(chatService.getChatMessagesWithPaging(chatRoomId, pageLastId, pageSize));
    }

    @MessageMapping("/message/{chatRoomId}")
    @SendTo("/sub/chat/{chatRoomId}")
    public ChatMessageResponse sendMessage(
            @DestinationVariable("chatRoomId") Long chatRoomId,
            @Payload ChatMessageRequest chatMessageRequest,
            @Header("Authorization") String accessToken
            ) {
        log.debug("[sendMessage] chatRoomId: {}", chatRoomId);
        log.debug("[sendMessage] chatMessageRequest: {}", chatMessageRequest);
        log.debug("[sendMessage] accessToken: {}", accessToken);
        return chatService.sendMessage(chatRoomId, accessToken, chatMessageRequest);
    }

}
