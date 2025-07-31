package com.thred.datingapp.common.entity.card;

import com.thred.datingapp.common.entity.BaseEntity;
import com.thred.datingapp.common.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;

@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Card extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("카드 유저 & 오픈된 카드 유저")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="profile_user_id")
    private User profileUser;

    @Comment("오픈된 카드 정보")
    @OneToMany(mappedBy = "card", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<CardOpen> cardOpens = new ArrayList<>();

    @Builder
    public Card(User profileUser) {
        this.profileUser = profileUser;
    }
}
