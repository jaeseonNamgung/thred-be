package com.thred.datingapp.community.repository;

import com.thred.datingapp.common.entity.community.Community;
import com.thred.datingapp.community.repository.queryDsl.CommunityQueryDsl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface CommunityRepository extends JpaRepository<Community, Long>, CommunityQueryDsl {

    Optional<Community> findById(Long communityId);

    @Modifying
    @Query("update Community c set c.user = null where c.user.id = :userId")
    void detachUserFromCommunities(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Community c where c.id = :communityId")
    void deleteByCommunityId(@Param("communityId") Long communityId);
}

