package com.thred.datingapp.common.entity.chat;

import com.thred.datingapp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@ToString(exclude = {"chatPart"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Chat extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    private boolean readStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatPart_id")
    private ChatPart chatPart;

    @Builder
    public Chat(String message, boolean readStatus, ChatPart chatPart) {
        this.message = message;
        this.readStatus = readStatus;
        addChatPart(chatPart);
    }


    public void addChatPart(ChatPart chatPart){
        if(this.chatPart != null){
            this.chatPart.getChats().remove(this);
        }
        this.chatPart = chatPart;
        this.chatPart.getChats().add(this);
    }
}
