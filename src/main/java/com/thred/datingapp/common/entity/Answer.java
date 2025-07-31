package com.thred.datingapp.common.entity;

import com.thred.datingapp.common.entity.user.Question;
import com.thred.datingapp.common.entity.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
public class Answer extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    private String answer1;
    @Lob
    private String answer2;
    @Lob
    private String answer3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="receiver_id")
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="question_id")
    private Question question;

    public Answer() {}

    @Builder
    public Answer(String answer1, String answer2, String answer3, User sender, User receiver, Question question) {
        this.answer1 = answer1;
        this.answer2 = answer2;
        this.answer3 = answer3;
        this.sender = sender;
        this.receiver = receiver;
        this.question=question;
    }

    public void updateAnswer(String answer1, String answer2, String answer3){
        this.answer1=answer1;
        this.answer2=answer2;
        this.answer3=answer3;
    }
}
