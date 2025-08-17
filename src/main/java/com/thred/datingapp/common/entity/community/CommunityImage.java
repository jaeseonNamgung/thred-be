package com.thred.datingapp.common.entity.community;

import com.thred.datingapp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class CommunityImage extends BaseEntity {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, name = "s3_path")
  private String s3Path;

  @Column(nullable = false)
  private String originalFileName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "community_id")
  private Community community;

  @Builder
  public CommunityImage(String s3Path, String originalFileName, Community community) {
    this.s3Path = s3Path;
    this.originalFileName = originalFileName;
    addCommunity(community);
  }

  public void addCommunity(Community community) {
    if(this.community != null) {
      this.community.getCommunityImages().remove(this);
    }
    this.community = community;
    this.community.getCommunityImages().add(this);
  }


}
