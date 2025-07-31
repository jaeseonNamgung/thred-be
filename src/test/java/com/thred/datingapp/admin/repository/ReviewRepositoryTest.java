package com.thred.datingapp.admin.repository;

import com.testFixture.UserFixture;
import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.admin.Review;
import com.thred.datingapp.common.entity.admin.ReviewType;
import com.thred.datingapp.common.entity.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({P6SpyConfig.class, JpaConfig.class})
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("deleteByUserIdAndJudgmentType - 해당 userId와 검증 유형을 고려하여 삭제한다.")
    void deleteByUserIdAndReviewType_SUCCESS() {
        // given
        User user = UserFixture.createCertificationUser1();
        em.persist(user);
        Review review = Review.builder().user(user).reviewType(ReviewType.EDIT_INTRODUCE).build();
        em.persist(review);

        em.flush();
        em.clear();
        // when
        reviewRepository.deleteByUserIdAndReviewType(user.getId(), ReviewType.EDIT_INTRODUCE);
        Optional<Review> findJudgments = reviewRepository.findById(review.getId());
        // then
        assertThat(findJudgments).isEmpty();
    }

    @Test
    @DisplayName("deleteSameFJudgmentsByUserId - 해당 userId와 검증 유형을 고려하여 삭제한다. 검증 유형이 다르면 삭제하지 않는다.")
    void deleteByUserIdAndReviewType_Fail() {
        // given
        User user = UserFixture.createCertificationUser1();
        em.persist(user);
        Review judgments = Review.builder().user(user).reviewType(ReviewType.EDIT_PROFILE).build();
        em.persist(judgments);
        // when
        reviewRepository.deleteByUserIdAndReviewType(user.getId(), ReviewType.EDIT_INTRODUCE);
        Optional<Review> findJudgments = reviewRepository.findByIdFetchUser(user.getId());
        // then
        assertThat(findJudgments).isPresent();
        Review review = findJudgments.get();
        assertThat(review.getReviewType()).isEqualTo(ReviewType.EDIT_PROFILE);
    }
}
