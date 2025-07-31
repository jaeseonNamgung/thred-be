package com.thred.datingapp.community.repository;


import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.community.CommunityImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@Sql({"classpath:db/data.sql"})
@Import({JpaConfig.class, P6SpyConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class CommunityImageRepositoryTest {

    @Autowired
    private CommunityImageRepository communityImageRepository;

    @Test
    @DisplayName("[JPA] 커뮤니티 아이디로 S3 이미지 조회")
    void findByCommunityIdTestCase1() {
        // given

        // when
        List<CommunityImage> expectedS3Images = communityImageRepository.findByCommunityId(1L);
        // then
        assertThat(expectedS3Images.size()).isEqualTo(2);
        assertThat(expectedS3Images.get(0).getS3Path()).isEqualTo("community1_img1.jpg");
        assertThat(expectedS3Images.get(1).getS3Path()).isEqualTo("community1_img2.jpg");
    }

    @Test
    @DisplayName("[JPA] 커뮤니티 아이디로 S3 이미지 조회")
    void deleteByCommunityIdTestCase1() {
        // given

        // when
        communityImageRepository.deleteByCommunityId(1L);
        List<CommunityImage> expectedS3Images = communityImageRepository.findByCommunityId(1L);
        // then
        assertThat(expectedS3Images.isEmpty()).isTrue();
    }

}
