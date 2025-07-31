package com.thred.datingapp.community.repository.queryDsl;

public interface CommunityLikeQueryDsl {
  boolean existsLikesByCommunityIdAndUserId(Long communityId, Long userId);
}
