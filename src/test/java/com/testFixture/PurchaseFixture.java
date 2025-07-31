package com.testFixture;

import com.thred.datingapp.common.entity.inApp.Product;
import com.thred.datingapp.common.entity.inApp.Receipt;
import com.thred.datingapp.common.entity.inApp.ThreadUseHistory;
import com.thred.datingapp.common.entity.inApp.UserAsset;
import com.thred.datingapp.common.entity.inApp.type.InAppType;
import com.thred.datingapp.common.entity.inApp.type.PurchaseType;
import com.thred.datingapp.common.entity.inApp.type.TransactionType;
import com.thred.datingapp.common.entity.user.User;

import java.time.LocalDateTime;

public class PurchaseFixture {

  public static Product createProduct() {
    return Product.builder().inAppProductId("123456").title("sample product").price(10000).quantityThread(10).build();
  }
  public static Receipt createReceipt(User user, Product product, UserAsset userAsset) {

    return Receipt.builder()
                  .transactionId("tx_123456789")
                  .originalTransactionId("original_tx_987654321")
                  .inAppType(InAppType.GOOGLE)
                  .transactionReason(TransactionType.PURCHASE)
                  .purchaseDate(LocalDateTime.now().minusDays(3))
                  .user(user)
                  .product(product)
                  .userAsset(userAsset)
                  .build();

  }

  public static UserAsset createUserAsset(User user) {
    return UserAsset.builder().totalThread(100).user(user).build();
  }

  public static ThreadUseHistory createThreadUserHistory(User user) {
    return ThreadUseHistory.builder().purchaseTargetUserId(user.getId()).purchaseType(PurchaseType.VIEW_PROFILE).user(user).build();
  }
}
