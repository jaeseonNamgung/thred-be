package com.thred.datingapp.common.entity.chat;

import com.thred.datingapp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class ChatRoom extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "chatRoom")
    private List<ChatPart> chatParts = new ArrayList<>();

    public static ChatRoom createChatRoom() {
        return new ChatRoom();
    }

}
