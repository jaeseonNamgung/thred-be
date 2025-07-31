package com.thred.datingapp.common.entity.user;

import com.thred.datingapp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Question extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String question1;

    @Column(nullable = false)
    private String question2;

    @Column(nullable = false)
    private String question3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Question(String question1,String question2,String question3,User user){
        this.question1=question1;
        this.question2=question2;
        this.question3=question3;
        this.user=user;
    }

}
