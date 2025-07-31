package com.thred.datingapp.main.dto.response;

import com.thred.datingapp.common.entity.card.Card;

public record CardResponse(
        Long cardId,
        Long memberId,
        String mainProfile,
        boolean isOpen,
        boolean connect
) {
    public static CardResponse of(Long cardId, Long memberId, String url, boolean open) {
        return new CardResponse(cardId, memberId, url, open,false);
    }

    public static CardResponse of(Card card) {
        boolean isOpen = card.getCardOpens() != null && !card.getCardOpens().isEmpty();
        return new CardResponse(card.getId(), card.getProfileUser().getId(), card.getProfileUser().getMainProfile(),isOpen, false);
    }
}
