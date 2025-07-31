package com.thred.datingapp.common.entity.inApp;

import com.thred.datingapp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Product extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String inAppProductId;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private Integer price;
    @Column(nullable = false)
    private Integer quantityThread;

    @Builder
    public Product(String inAppProductId, String title, Integer price, Integer quantityThread) {
        this.inAppProductId = inAppProductId;
        this.title = title;
        this.price = price;
        this.quantityThread = quantityThread;
    }
}
