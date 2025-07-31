package com.thred.datingapp.chat.repository.queryDsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thred.datingapp.common.entity.chat.ChatPart;
import com.thred.datingapp.common.entity.chat.QChatPart;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.thred.datingapp.common.entity.chat.QChatPart.chatPart;


@RequiredArgsConstructor
public class ChatPartQueryDslImpl implements ChatPartQueryDsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatPart> findByRoomId(Long roomId) {
        return queryFactory.selectFrom(chatPart)
                .join(chatPart.user).fetchJoin()
                .where(chatPart.chatRoom.id.eq(roomId))
                .fetch();
    }

    @Override
    public Optional<ChatPart> findByChatRoomIdAndOtherConnectorId(Long chatRoomId, Long otherConnectorId) {
        ChatPart fetchChatPart = queryFactory.selectFrom(chatPart)
            .distinct()
            // 채팅 메시지는 없을 수 있기 때문에 leftJoin 으로 설정
            .leftJoin(chatPart.chats).fetchJoin()
            .where(chatPart.chatRoom.id.eq(chatRoomId).and(chatPart.user.id.ne(otherConnectorId)))
            .fetchOne();

        return Optional.ofNullable(fetchChatPart);
    }


    @Override
    public Optional<ChatPart> findByChatRoomIdAndUserId(Long chatRoomId, Long userId) {
        ChatPart chatPart = queryFactory.selectFrom(QChatPart.chatPart)
            .where(QChatPart.chatPart.chatRoom.id.eq(chatRoomId), QChatPart.chatPart.user.id.eq(userId))
            .fetchOne();
        return Optional.ofNullable(chatPart);
    }

    @Override
    public List<ChatPart> findByChatRoomId(Long chatRoomId) {
        return queryFactory.selectFrom(chatPart)
                .join(chatPart.user).fetchJoin()
                .where(chatPart.chatRoom.id.eq(chatRoomId))
                .fetch();
    }
}
