package com.thred.datingapp.common.entity.user;

import com.thred.datingapp.common.entity.BaseEntity;
import com.thred.datingapp.common.entity.user.field.*;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "user_info")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long socialId;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserState userState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private String email;

    @Embedded
    private Address address;

    @Lob
    @Column(nullable = false)
    private String introduce;

    private String code;

    private String inputCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerGender partnerGender;

    private String phoneNumber;

    private Boolean certification;

    private LocalDate withdrawRequestDate;

    @Column(nullable = false)
    private String mainProfile;

    @Setter(AccessLevel.PROTECTED)
    @OneToOne(mappedBy = "user")
    private UserDetail userDetail;
    @OneToMany(mappedBy = "blocker")
    private List<Block> blocks = new ArrayList<>();
    @OneToMany(mappedBy = "user")
    private List<Picture> profiles = new ArrayList<>();


    private User(Long socialId, Long userId, Role role) {
        this.socialId = socialId;
        this.id = userId;
        this.role = role;
    }

    @Builder
    public User(String username, Long socialId, LocalDate birth, Role role, Gender gender, String email,
                Address address, String introduce, String code, PartnerGender partnerGender,
                String phoneNumber, Boolean certification, String inputCode, String mainProfile) {
        this.username = username;
        this.socialId = socialId;
        this.userState = UserState.ACTIVE;
        this.birth = birth;
        this.role = role;
        this.gender = gender;
        this.email = email;
        this.address = address;
        this.introduce = introduce;
        this.code = code;
        this.partnerGender = partnerGender;
        this.phoneNumber = phoneNumber;
        this.certification = certification;
        this.inputCode = inputCode;
        this.mainProfile = mainProfile;
    }

    public void createCode(String code) {
        this.code = code;
    }

    public static User createUserForJwt(Long socialId, Long userId, String role) {
        return new User(socialId, userId, Role.findRole(role));
    }

    public void updateMainProfile(String path) {
        this.mainProfile = path;
    }

    public void updateIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public void requestWithdraw() {
        this.userState = UserState.WITHDRAW_REQUESTED;
        this.withdrawRequestDate = LocalDate.now();
    }

    public void cancelWithdraw() {
        if (this.userState == UserState.WITHDRAW_REQUESTED &&
            this.withdrawRequestDate != null &&
            this.withdrawRequestDate.plusDays(30).isAfter(LocalDate.now())) {

            this.userState = UserState.ACTIVE;
            this.withdrawRequestDate = null;
        }
    }

    public void successJoin() {
        this.certification = true;
    }

    public void failJoin() {
        this.certification = false;
    }

    public void changePhoneNumber(String number) {
        this.phoneNumber = number;
    }

    public void changeAddress(String city, String address) {
        this.address = Address.of(city, address);
    }

    public int getAge() {
        LocalDate currentDate = LocalDate.now();
        return Period.between(birth, currentDate).getYears();
    }

    public void updateUserState(UserState userState) {
        this.userState = userState;
    }
}
