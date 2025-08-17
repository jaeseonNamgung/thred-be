package com.thred.datingapp.common.entity.community;

import com.thred.datingapp.common.entity.BaseEntity;
import com.thred.datingapp.common.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@ToString(callSuper = true, exclude = {"community", "user"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Comment extends BaseEntity {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 1000)
    private String content;
    @Column(nullable = false)
    private boolean isPublicProfile;
    @Column(nullable = false)
    private boolean isDelete;

    private Long parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="community_id")
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Comment(String content, User user, boolean isPublicProfile, Community community, Long parentId) {
        this.content = content;
        this.user = user;
        this.isPublicProfile = isPublicProfile;
        this.isDelete = false;
        this.parentId = parentId;
        addCommunity(community);
    }

    public void addCommunity(Community community) {
        if(this.community != null) {
            this.community.getComments().remove(this);
        }
        this.community = community;
        this.community.getComments().add(this);
    }

    public void deleteComment(String deleteMessage) {
        this.isDelete = true;
        this.content = deleteMessage;
    }

}
