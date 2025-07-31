package com.thred.datingapp.common.entity.user.field;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Embeddable
public class Address {
    private String city;
    private String province;

    private Address(String city, String province) {
        this.city = city;
        this.province = province;
    }

    public static Address of(String city, String province) {
        return new Address(city, province);
    }
}
