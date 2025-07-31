package com.thred.datingapp.community.repository;

import com.thred.datingapp.common.entity.community.CommunityImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityImageRepository extends JpaRepository<CommunityImage, Long>  {

    @Query("select ci.s3Path from CommunityImage ci where ci.community.id = :communityId")
    List<String> findS3PathByCommunityId(@Param("communityId") Long communityId);

    @Query("select ci from CommunityImage ci where ci.community.id = :communityId")
    List<CommunityImage> findByCommunityId(@Param("communityId") Long communityId);

    @Modifying(clearAutomatically = true)
    @Query("delete from CommunityImage ci where ci.community.id = :communityId")
    void deleteByCommunityId(@Param("communityId") Long communityId);

    @Modifying(clearAutomatically = true)
    @Query("delete from CommunityImage ci where ci.id = :imageId")
    void deleteByImageId(@Param("imageId") Long imageId);
}
