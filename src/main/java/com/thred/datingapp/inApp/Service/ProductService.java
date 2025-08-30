package com.thred.datingapp.inApp.Service;

import com.thred.datingapp.common.entity.inApp.Product;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.InAppErrorCode;
import com.thred.datingapp.inApp.dto.response.ProductResponse;
import com.thred.datingapp.inApp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProductService {

  private final ProductRepository productRepository;

  public Product getByProductId(final String productId) {
    return productRepository.findByInAppProductId(productId).orElseThrow(() -> {
      log.error("[getByProductId] 존재하지 않은 상품입니다.(Not exist product) ===> productId: {}", productId);
      return new CustomException(InAppErrorCode.PURCHASE_ERROR);
    });
  }

  public List<ProductResponse> getProductAll() {
    return productRepository.findAll().stream().map(ProductResponse::toEntity).toList();
  }
}
