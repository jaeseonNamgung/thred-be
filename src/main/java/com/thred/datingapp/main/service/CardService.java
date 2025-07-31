package com.thred.datingapp.main.service;

import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.common.entity.card.Card;
import com.thred.datingapp.common.entity.card.CardOpen;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.field.Gender;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.MainErrorCode;
import com.thred.datingapp.common.utils.RedisUtils;
import com.thred.datingapp.main.dto.response.CardOpenResponse;
import com.thred.datingapp.main.repository.CardOpenRepository;
import com.thred.datingapp.main.repository.CardRepository;
import com.thred.datingapp.user.api.response.CardProfileResponse;
import com.thred.datingapp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final UserService userService;
    private final CardRepository     cardRepository;
    private final CardOpenRepository cardOpenRepository;

    private final RedisUtils redisUtils;

    @Transactional
    public void createCard(Long profileUserId) {
        checkDuplicateCard(profileUserId);
        User profileUser = userService.getUserById(profileUserId);
        Card card = Card.builder().profileUser(profileUser).build();
        cardRepository.save(card);
        log.info("[createCard] 카드 생성 완료 ===> cardId: {}", card.getId());
    }

    public PageResponse<CardProfileResponse> getTodayRandomCard(Long viewerId, String city, long pageLastId, int pageSize) {
        User user = userService.getUserById(viewerId);
        Gender gender = user.getGender();
        String key = "card:daily:viewer:" + viewerId + ":" + LocalDate.now();
        List<CardProfileResponse> randomCards = (List<CardProfileResponse>) redisUtils.getValue(key);
        if(randomCards != null && !randomCards.isEmpty()) {
            return extractDetails(randomCards, pageLastId, pageSize);
        }

        randomCards = cardRepository.findTodayRandomCardByViewerIdGenderCity(viewerId, gender, city);
        long secondsUntilMidnight = getSecondsUntilMidnight();
        redisUtils.saveWithTTL(key, randomCards, secondsUntilMidnight, TimeUnit.SECONDS);
        return extractDetails(randomCards, pageLastId, pageSize);
    }

    public PageResponse<CardOpenResponse> getOpenCards(Long profileUserId, long pageLastId, int pageSize) {
        Page<CardOpenResponse> cardOpenResponsePage = cardOpenRepository.findCardOpenResponseByOpenerIdWithPaging(profileUserId, pageLastId, pageSize);
        return PageResponse.of(pageSize, cardOpenResponsePage.isLast(), cardOpenResponsePage.getContent());
    }

    @Transactional
    public void saveCardOpen(Long openerId, Long profileUserId) {
        Card card = getCardByProfileUserId(profileUserId);
        User opener = userService.getUserById(openerId);

        // 오픈된 카드가 있는지 체크
        existsOpenCard(opener.getId(), card.getId());

        CardOpen cardOpen = CardOpen.builder().card(card).opener(opener).build();
        cardOpenRepository.save(cardOpen);
        log.info("[saveCardOpen] CardOpen 저장 완료 ===> cardOpenId: {}", cardOpen.getId());
    }

    private void existsOpenCard(Long openerId, Long cardId) {
        boolean hasOpenCard = cardOpenRepository.existsByOpenerIdAndCardId(openerId, cardId);
        if(hasOpenCard){
            log.error("[existsOpenCard] 이미 오픈한 카드입니다. openerId: {}, cardId: {} ", openerId, cardId);
            throw new CustomException(MainErrorCode.ALREADY_OPENED_CARD);
        }
    }

    @Transactional
    public void deleteCardOpen(Long openerId, Long profileUserId) {
        Card card = getCardByProfileUserId(profileUserId);
        cardOpenRepository.deleteByOpenerIdAndCardId(openerId, card.getId());
        log.debug("[deleteCardOpen] 오픈된 카드 제거 ===> cardId: {}", card.getId());
    }


    @Transactional
    public void deleteAllCardsForWithdrawnUser(Long userId){
        Card card = getCardByProfileUserId(userId);

        // 탈퇴한 회원의 ID(userId) 기준으로:
        // 1. 회원이 오픈한 카드(CardOpen.opener) 또는
        // 2. 해당 회원의 카드(CardOpen.card)의 오픈 기록 전체 삭제
        cardOpenRepository.deleteAllByCardIdOrOpenerId(card.getId(), userId);
        cardRepository.deleteById(card.getId());
        log.debug("[deleteCardsForWithdrawnUser] 회원 탈퇴 카드 삭제 완료 ===> userId: {}", userId);
    }

    private long getSecondsUntilMidnight(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, midnight).getSeconds();
    }

    private PageResponse<CardProfileResponse> extractDetails(List<CardProfileResponse> cards, long pageLastId, int pageSize) {
        // pageLastId 가 0일 처음 페이지 조회
        int startIndex = 0;
        if(pageLastId > 0) {
            startIndex = IntStream.range(0, cards.size()).filter(i -> cards.get(i).getCardId().equals(pageLastId)).findFirst().orElse(-1);
            // pageLastId 다음 페이지 부터 조회하기 위해서 +1
            startIndex = startIndex + 1;
        }

        if (startIndex < 0) {
            log.error("[extractDetails] 잘못된 페이지 요청입니다. ===> pageIndex: {}, pageSize: {}", pageLastId, pageSize);
            throw new CustomException(MainErrorCode.NO_MORE_TODAY_CARDS);
        }
        int endIndex = Math.min(startIndex + pageSize, cards.size());
        List<CardProfileResponse> subCards = cards.subList(startIndex, endIndex);
        if (endIndex == cards.size()) {
            return PageResponse.of(subCards.size(), true, subCards);
        }
        return PageResponse.of(subCards.size(), false, subCards);
    }

    private Card getCardById(Long cardId) {
        return cardRepository.findById(cardId).orElseThrow(() -> {
            log.error("[getCardByProfileUserId] 존재하지 않은 카드입니다. ===> cardId: {}", cardId);
            return new CustomException(MainErrorCode.CARD_NOT_FOUND);
        });
    }

    private Card getCardByProfileUserId(Long userId) {
        return cardRepository.findByProfileUserId(userId).orElseThrow(() -> {
            log.error("[getCardByProfileUserId] 존재하지 않은 카드입니다. ===> profileUserId: {}", userId);
            return new CustomException(MainErrorCode.CARD_NOT_FOUND);
        });
    }

    private void checkDuplicateCard(Long profileUserId) {
        if (cardRepository.existsByProfileUserId(profileUserId)) {
            log.error("[checkDuplicateCard] 이미 생성된 카드입니다. ===> openerId: {}", profileUserId);
            throw new CustomException(MainErrorCode.CARD_ALREADY_CREATED);
        }
    }



}
