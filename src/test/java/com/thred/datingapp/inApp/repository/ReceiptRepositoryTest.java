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
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Import({JpaConfig.class, P6SpyConfig.class})
@DataJpaTest
class ReceiptRepositoryTest {

  @Autowired
  private ReceiptRepository receiptRepository;
  @Autowired
  private EntityManager     entityManager;

  @Test
  void deleteByUserId () {
    setUp(10);
    List<Receipt> receipts =
        entityManager.createQuery("select r from Receipt r where r.user.id = :userId", Receipt.class).setParameter("userId", 1L).getResultList();
    entityManager.flush();
    entityManager.clear();
    assertThat(receipts.size()).isEqualTo(10);
    receiptRepository.deleteByUserId(1L);
    receipts =
        entityManager.createQuery("select r from Receipt r where r.user.id = :userId", Receipt.class).setParameter("userId", 1L).getResultList();
    assertThat(receipts).isNullOrEmpty();
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
