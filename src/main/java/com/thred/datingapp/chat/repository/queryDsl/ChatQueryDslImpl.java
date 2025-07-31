package com.thred.datingapp.chat.repository.queryDsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thred.datingapp.common.entity.chat.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.thred.datingapp.common.entity.chat.QChat.chat;

@RequiredArgsConstructor
public class ChatQueryDslImpl implements ChatQueryDsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Chat> findChatsByChatRoomIdWithPaging(Long chatRoomId, Long pageLastId, int pageSize) {
        List<Chat> content = queryFactory.selectFrom(chat)
                .distinct()
                .join(chat.chatPart).fetchJoin()
                .join(chat.chatPart.chatRoom).fetchJoin()
                .join(chat.chatPart.user).fetchJoin()
                .where(chat.chatPart.chatRoom.id.eq(chatRoomId), pageLastIdCondition(pageLastId))
                .limit(pageSize)
                .orderBy(chat.createdDate.desc())
                .fetch();

        JPAQuery<Long> countFetch =
                queryFactory.select(chat.count()).from(chat).where(chat.chatPart.chatRoom.id.eq(chatRoomId));

        return PageableExecutionUtils.getPage(content, PageRequest.of(0, pageSize), countFetch::fetchOne);
    }

    @Override
    public List<Chat> findChatsByRoomId(Long chatRoomId, Long senderId) {
        return queryFactory.selectFrom(chat)
                .distinct()
                .join(chat.chatPart).fetchJoin()
                .join(chat.chatPart.user).fetchJoin()
                .where(chat.chatPart.chatRoom.id.eq(chatRoomId))
                .orderBy(chat.createdDate.desc())
                .fetch();
    }

    @Override
    public Long countUnReadChatMessageByReceiverId(Long receiverId) {
        return queryFactory.select(chat.count())
                .from(chat)
                .where(chat.chatPart.user.id.eq(receiverId), chat.readStatus.isFalse())
                .fetchOne();
    }

    private BooleanExpression pageLastIdCondition(Long pageLastId) {
        return pageLastId != null && pageLastId > 0 ? chat.id.lt((pageLastId)) : null;
    }
}
