package com.thred.datingapp.common.entity.community;

import com.thred.datingapp.common.entity.BaseEntity;
import com.thred.datingapp.common.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@ToString(callSuper = true, exclude = {"communityImages", "user", "comments"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Community extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(nullable = false)
    private Boolean isPublicProfile;

    @OneToMany(mappedBy = "community")
    private List<CommunityImage> communityImages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "community", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private final List<Comment> comments = new ArrayList<>();

    @Builder
    public Community(String title, String content, Boolean isPublicProfile, User user) {
        this.title = title;
        this.content = content;
        this.isPublicProfile = isPublicProfile;
        this.user = user;
    }

    public void updateBoard(String title, String content, Boolean isPublicProfile) {
        this.title = title;
        this.content = content;
        this.isPublicProfile = isPublicProfile;
    }
}
