package com.thred.datingapp.community.repository.queryDsl;

import com.thred.datingapp.common.entity.community.Community;
import com.thred.datingapp.common.entity.community.type.CommunityType;
import com.thred.datingapp.community.dto.response.CommunityAllResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface CommunityQueryDsl {
  Optional<Community> findByCommunityId(Long communityId);
  Page<CommunityAllResponse> findCommunitiesByCommunityTypeAndPageLastIdWithPaging(CommunityType communityType, Long pageLastId, int pageSize);
  Page<CommunityAllResponse> findCommunitiesByUseIdAndPageLastIdWithPaging(Long userId, Long pageLastId, int pageSize);
  @Modifying(clearAutomatically = true)
  Long updateByCommunityId(Community community);

  boolean existsByCommunityIdAndUserId(Long communityId, Long userId);


}
