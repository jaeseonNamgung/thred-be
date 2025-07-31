package com.thred.datingapp.inApp.dto.response;

import com.thred.datingapp.common.entity.inApp.Product;

public record ProductResponse(
    Long id,
    String inAppProductId,
    String title,
    Integer price,
    Integer quantityThread
) {
  public static ProductResponse of(Long id, String inAppProductId, String title, Integer price, Integer quantityThread) {
    return new ProductResponse(id, inAppProductId, title, price, quantityThread);
  }
  public static ProductResponse toEntity(Product product){
    return ProductResponse.of(product.getId(), product.getInAppProductId(), product.getTitle(), product.getPrice(), product.getQuantityThread());
  }
}
