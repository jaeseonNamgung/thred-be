package com.thred.datingapp.common.entity.card;

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
public class CardOpen extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opener_id")
  private User opener;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "card_id")
  private Card card;


  @Builder
  public CardOpen(User opener, Card card) {
    this.opener = opener;
    addCard(card);
  }

  private void addCard(Card card) {
    if(this.card != null) {
      this.card.getCardOpens().remove(this);
    }
    this.card = card;
    card.getCardOpens().add(this);
  }
}
