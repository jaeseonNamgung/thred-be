package com.thred.datingapp.inApp.repository;

import com.testFixture.PurchaseFixture;
import com.testFixture.UserFixture;
import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.inApp.Product;
import com.thred.datingapp.common.entity.inApp.Receipt;
import com.thred.datingapp.common.entity.inApp.ThreadUseHistory;
import com.thred.datingapp.common.entity.inApp.UserAsset;
import com.thred.datingapp.common.entity.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@Import({JpaConfig.class, P6SpyConfig.class})
@ActiveProfiles("test")
@DataJpaTest
class UserAssetRepositoryTest {

    @Autowired
    private ReceiptRepository receiptRepository;
    @Autowired
    private UserAssetRepository userAssetRepository;
    @Autowired
    private ThreadUseHistoryRepository threadUseHistoryRepository;
    @Autowired
    private EntityManager       entityManager;


    @Test
    void deleteByUserId () {
        setUp(10);
        List<Receipt> receipts =
            entityManager.createQuery("select r from Receipt r where r.user.id = :userId", Receipt.class).setParameter("userId", 1L).getResultList();
        List<ThreadUseHistory> threadUseHistories =
            entityManager.createQuery("select t from ThreadUseHistory t where t.user.id = :userId", ThreadUseHistory.class).setParameter("userId", 1L).getResultList();
        List<UserAsset> userAssets =
            entityManager.createQuery("select ua from UserAsset ua where ua.user.id = :userId", UserAsset.class).setParameter("userId", 1L).getResultList();
        entityManager.flush();
        entityManager.clear();
        assertThat(receipts.size()).isEqualTo(10);
        assertThat(threadUseHistories.size()).isEqualTo(10);
        assertThat(userAssets.size()).isEqualTo(1);
        receiptRepository.deleteByUserId(1L);
        threadUseHistoryRepository.deleteByUserId(1L);
        userAssetRepository.deleteByUserId(1L);
        receipts =
            entityManager.createQuery("select r from Receipt r where r.user.id = :userId", Receipt.class).setParameter("userId", 1L).getResultList();
        threadUseHistories =
            entityManager.createQuery("select t from ThreadUseHistory t where t.user.id = :userId", ThreadUseHistory.class).setParameter("userId", 1L).getResultList();
        userAssets =
            entityManager.createQuery("select ua from UserAsset ua where ua.user.id = :userId", UserAsset.class).setParameter("userId", 1L).getResultList();
        assertThat(receipts).isNullOrEmpty();
        assertThat(threadUseHistories).isNullOrEmpty();
        assertThat(userAssets).isNullOrEmpty();
    }

    private void setUp(int count){
        User user = UserFixture.createTestUser(1);
        entityManager.persist(user);
        UserAsset userAsset = PurchaseFixture.createUserAsset(user);
        entityManager.persist(userAsset);
        Product product = PurchaseFixture.createProduct();
        entityManager.persist(product);
        for (int i = 1; i <= count; i++) {
            Receipt receipt = PurchaseFixture.createReceipt(user, product, userAsset);
            entityManager.persist(receipt);
            ThreadUseHistory threadUserHistory = PurchaseFixture.createThreadUserHistory(user);
            entityManager.persist(threadUserHistory);
        }
        entityManager.flush();
        entityManager.clear();
    }

}
