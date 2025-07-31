package com.thred.datingapp.common.entity.user;

import com.thred.datingapp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Block extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_user_id")
    @Comment("차단당한 사용자")
    private User blockedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id")
    @Comment("차단한 사용자")
    private User blocker;


    @Builder
    public Block(User blockedUser, User blocker) {
        this.blockedUser = blockedUser;
        addBlock(blocker);
    }

    private void addBlock(User blocker) {
        if(this.blocker != null) {
            this.blocker.getBlocks().remove(this);
        }
        this.blocker = blocker;
        blocker.getBlocks().add(this);
    }
}
