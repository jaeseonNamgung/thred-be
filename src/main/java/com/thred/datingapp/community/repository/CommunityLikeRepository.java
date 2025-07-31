package com.thred.datingapp.community.repository;

import com.thred.datingapp.common.entity.community.CommunityLike;
import com.thred.datingapp.community.repository.queryDsl.CommunityLikeQueryDsl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityLikeRepository extends JpaRepository<CommunityLike, Long> , CommunityLikeQueryDsl {

  /*
  * @Author NamgungJaeseon
  * @Date 2025.01.19
  * @Description @Modifying(clearAutomatically = true)을 사용하여 영속성 컨텍스트에
  *               수정된 값을 DB에 저장하여 조회시 수정된 값을 사용하기 위해 사용
  * */
  @Modifying(clearAutomatically = true)
  @Query(value = "INSERT INTO community_like(community_id, user_id, created_date) VALUES(:communityId, :userId, now())", nativeQuery = true)
  void insertLikeByCommunityIdAndUserId(@Param("communityId")Long communityId, @Param("userId")Long userId);

  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM CommunityLike WHERE communityLikePk.communityId = :communityId AND communityLikePk.userId = :userId")
  void deleteLikeByCommunityIdAndUserId(@Param("communityId")Long communityId, @Param("userId")Long userId);

  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM CommunityLike WHERE communityLikePk.communityId = :communityId")
  void deleteLikeByCommunityId(@Param("communityId") Long communityId);
}
