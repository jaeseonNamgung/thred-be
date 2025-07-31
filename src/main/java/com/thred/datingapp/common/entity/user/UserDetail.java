package com.thred.datingapp.common.entity.user;

import com.thred.datingapp.common.entity.BaseEntity;
import com.thred.datingapp.common.entity.user.field.Belief;
import com.thred.datingapp.common.entity.user.field.Drink;
import com.thred.datingapp.common.entity.user.field.Job;
import com.thred.datingapp.common.entity.user.field.Mbti;
import com.thred.datingapp.common.entity.user.field.OppositeFriends;
import com.thred.datingapp.common.entity.user.field.Smoke;
import com.thred.datingapp.main.dto.request.EditDetailsRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class UserDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Integer height;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Drink drink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Belief belief;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Smoke smoke;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OppositeFriends oppositeFriends;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mbti mbti;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Job job;

    @Column(nullable = false)
    private Integer temperature;

    @Builder
    public UserDetail(User user, Integer height, Drink drink, Belief belief, OppositeFriends oppositeFriends, Smoke smoke,
                      Mbti mbti, Job job, int temperature) {
        addUser(user);
        this.height = height;
        this.drink = drink;
        this.belief = belief;
        this.oppositeFriends = oppositeFriends;
        this.smoke = smoke;
        this.mbti = mbti;
        this.job = job;
        this.temperature = temperature;
    }

    public void updateDetailsForJoin(UserDetail userDetail) {
        this.height = userDetail.getHeight();
        this.drink = userDetail.getDrink();
        this.belief = userDetail.getBelief();
        this.smoke = userDetail.getSmoke();
        this.mbti = userDetail.getMbti();
        this.job = userDetail.getJob();
        this.oppositeFriends = userDetail.getOppositeFriends();
        this.temperature = userDetail.getTemperature();
    }

    public void updateDetailsForEdit(EditDetailsRequest details) {
        this.height = details.height();
        this.drink = Drink.findDrink(details.drink());
        this.belief = Belief.findBelief(details.belief());
        this.smoke = Smoke.findSmoke(details.smoke());
        this.oppositeFriends = OppositeFriends.findOppositeFriends(details.oppositeFriends());
        this.job = Job.findJob(details.job());
        this.mbti = Mbti.findMbti(details.mbti());
        this.temperature = details.temperature();
    }

    private void addUser(User user) {
        this.user = user;
        user.setUserDetail(this);
    }
}
