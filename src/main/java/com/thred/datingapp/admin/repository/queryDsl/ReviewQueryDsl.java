package com.thred.datingapp.admin.repository.queryDsl;

import com.thred.datingapp.common.entity.admin.Review;
import com.thred.datingapp.common.entity.admin.ReviewStatus;
import org.springframework.data.domain.Page;

public interface ReviewQueryDsl {

  Page<Review> findByReviewStatusFetchUserOrderByCreatedDateDescWithPaging(ReviewStatus reviewStatus, Long pageLastId, int pageSize);
}
