package com.thred.datingapp.common.entity.chat;

import com.thred.datingapp.common.entity.BaseEntity;
import com.thred.datingapp.common.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@ToString(exclude = {"user", "chatRoom", "chats"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class ChatPart extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatRoom_id")
    private ChatRoom chatRoom;

    @OneToMany(mappedBy = "chatPart")
    private List<Chat> chats = new ArrayList<>();

    @Builder
    public ChatPart(User user, ChatRoom chatRoom) {
        this.user = user;
        addChatRoom(chatRoom);
    }

    private void addChatRoom(ChatRoom chatRoom) {
        if(this.chatRoom != null) {
            this.chatRoom.getChatParts().remove(this);
        }
        this.chatRoom = chatRoom;
        this.chatRoom.getChatParts().add(this);
    }
}
