package com.thred.datingapp.user.repository;

import com.thred.datingapp.common.entity.user.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PictureRepository extends JpaRepository<Picture, Long>{

  @Query("select p from Picture p where p.id in :ids")
  List<Picture> findByProfileIds(@Param("ids")List<Long> ids);

  @Modifying(clearAutomatically = true)
  @Query("delete from Picture p where p.id in :deleteFileIds")
  void deleteProfileByIds(@Param("deleteFileIds")List<Long> deleteFileIds);

  @Query("select p from Picture p where p.user.id = :userId and p.id not in :ids")
  List<Picture> findAllByUserIdAndIdNotIn(@Param("userId")Long userId, @Param("ids")List<Long> ids);

  @Query("select p from Picture p where p.user.id = :userId")
  List<Picture> findAllByUserId(@Param("userId")Long userId);

  @Query("select p from Picture p where p.user.id = :userId")
  List<Picture> findByUserId(@Param("userId")Long userId);

  @Query("select p.s3Path from Picture p where p.user.id = :userId")
  List<String> findS3PathAllByUserId(@Param("userId")Long userId);

  @Modifying(clearAutomatically = true)
  @Query("delete from Picture p where p.user.id = :userId")
  void deleteByUserId(@Param("userId")Long userId);


}
