package com.thred.datingapp.chat.repository.queryDsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thred.datingapp.common.entity.chat.ChatRoom;
import com.thred.datingapp.common.entity.chat.QChatPart;
import com.thred.datingapp.common.entity.chat.QChatRoom;
import com.thred.datingapp.common.entity.user.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Optional;

import static com.thred.datingapp.common.entity.chat.QChatPart.chatPart;
import static com.thred.datingapp.common.entity.chat.QChatRoom.chatRoom;
import static com.thred.datingapp.common.entity.user.QUser.user;

@RequiredArgsConstructor
public class ChatRoomQueryDslImpl implements ChatRoomQueryDsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existChatRoomByChatRoomId(Long id) {
        return queryFactory.selectOne()
                .from(chatRoom)
                .where(chatRoom.id.eq(id))
                .fetchFirst() != null;
    }

    public Page<ChatRoom> findChatRoomsByUserIdWithPagination(Long userId, Long pageLastId, Integer pageSize) {
        QChatPart subChatPart = QChatPart.chatPart;
        List<ChatRoom> content = queryFactory
                .selectFrom(chatRoom)
                .distinct()
                .join(chatRoom.chatParts, chatPart).fetchJoin()
                .join(chatPart.user, user).fetchJoin()
                .where(chatRoom.id.in(JPAExpressions
                                .select(subChatPart.chatRoom.id)
                                .from(subChatPart)
                                .where(subChatPart.user.id.eq(userId))),
                        pageLastIdCondition(pageLastId))
                .limit(pageSize)
                .orderBy(chatRoom.createdDate.desc())
                .fetch();
        JPAQuery<Long> fetchCount = queryFactory
                .select(chatRoom.id.countDistinct())
                .from(chatRoom)
                .where(chatRoom.id.in(JPAExpressions
                                .select(subChatPart.chatRoom.id)
                                .from(subChatPart)
                                .where(subChatPart.user.id.eq(userId))));
        return PageableExecutionUtils.getPage(content, Pageable.ofSize(pageSize), fetchCount::fetchOne);
    }

    @Override
    public Optional<ChatRoom> findChatRoomById(Long id) {
        ChatRoom chatRoom = queryFactory.selectFrom(QChatRoom.chatRoom)
                .distinct()
                .join(QChatRoom.chatRoom.chatParts, chatPart).fetchJoin()
                .join(chatPart.user, user).fetchJoin()
                .where(QChatRoom.chatRoom.id.eq(id))
                .fetchOne();
        return Optional.ofNullable(chatRoom);
    }

    @Override
    public Optional<ChatRoom> findBySenderIdAndReceiverId(Long senderId, Long receiverId) {
        QChatRoom chatRoom = QChatRoom.chatRoom;
        QChatPart chatPart = QChatPart.chatPart;
        QUser user = QUser.user;

        ChatRoom fetchChatRoom = queryFactory.selectFrom(chatRoom)
                .where(chatRoom.id.in(
                        JPAExpressions
                                .select(chatPart.chatRoom.id)
                                .from(chatPart)
                                .join(chatPart.user, user)
                                .where(user.id.in(senderId, receiverId))
                                .groupBy(chatPart.chatRoom.id)
                                .having(user.id.countDistinct().eq(2L))
                )).fetchOne();
        return Optional.ofNullable(fetchChatRoom);
    }

    @Override
    public Optional<ChatRoom> findByChatRoomIdAndUserId(Long chatRoomId, Long userId) {
        QChatPart chatPart = QChatPart.chatPart;
        ChatRoom chatRoom = queryFactory.selectFrom(QChatRoom.chatRoom)
                .leftJoin(QChatRoom.chatRoom.chatParts, chatPart).fetchJoin()
                .where(QChatRoom.chatRoom.id.eq(chatRoomId), chatPart.user.id.eq(userId))
                .fetchOne();
        return Optional.ofNullable(chatRoom);
    }

    private BooleanExpression pageLastIdCondition(Long pageLastId) {
        return pageLastId != null && pageLastId > 0 ? chatRoom.id.lt((pageLastId)) : null;
    }
}
