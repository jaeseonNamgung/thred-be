package com.thred.datingapp.common.entity.chat;

import com.thred.datingapp.common.entity.BaseEntity;
import com.thred.datingapp.common.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class FcmToken extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public FcmToken(String token, User member) {
        this.token = token;
        this.user = member;
    }
}
