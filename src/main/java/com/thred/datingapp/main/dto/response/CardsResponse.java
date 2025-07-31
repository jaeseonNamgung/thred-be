package com.thred.datingapp.main.dto.response;

import com.thred.datingapp.common.entity.card.Card;
import java.util.List;
import java.util.stream.Collectors;

public record CardsResponse(
        List<CardResponse> cards
) {
    public static CardsResponse of(List<Card> cards) {
        List<CardResponse> cardsResponses = cards.stream()
                .map(card -> CardResponse.of(
                    card.getId(),
                    card.getProfileUser().getId(),
                    card.getProfileUser().getMainProfile(),
                    card.getCardOpens() != null && !card.getCardOpens().isEmpty()
                ))
                .collect(Collectors.toList());
        return new CardsResponse(cardsResponses);
    }

}
