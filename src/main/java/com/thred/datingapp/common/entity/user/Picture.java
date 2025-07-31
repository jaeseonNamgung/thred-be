package com.thred.datingapp.common.entity.user;

import com.thred.datingapp.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
public class Picture extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "s3_path", nullable = false)
    @Comment("S3에 저장된 경로")
    private String s3Path;

    @Column(nullable = false)
    @Comment("Original 파일 이름")
    private String originalFileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Picture(String s3Path, String originalFileName, User user) {
        this.s3Path = s3Path;
        this.originalFileName = originalFileName;
        addUser(user);
    }

    private void addUser(User user) {
        if(this.user != null) {
            this.user.getProfiles().remove(this);
        }
        this.user = user;
        user.getProfiles().add(this);
    }
}
