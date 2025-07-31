package com.thred.datingapp.common.entity.inApp;

import com.thred.datingapp.common.entity.BaseEntity;
import com.thred.datingapp.common.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class UserAsset extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer totalThread;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public UserAsset(Integer totalThread, User user) {
        this.totalThread = totalThread;
        this.user = user;
    }

    public void quantityRemoveThread(int thread){
        if(this.totalThread < thread){
            this.totalThread = 0;
        }else{
            this.totalThread = this.totalThread - thread;
        }
    }

    public void quantityAddThread(int thread){
        this.totalThread = this.totalThread + thread;
    }

}
