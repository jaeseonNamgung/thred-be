package com.thred.datingapp.user.repository;

import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.user.Picture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import({JpaConfig.class, P6SpyConfig.class})
@ActiveProfiles("test")
@DataJpaTest
class PictureRepositoryTest {

  @Autowired
  private PictureRepository pictureRepository;

  @Test
  void findByProfileIdsTest() {
    // given
    List<Picture> pictures = createPictures(3);
    pictureRepository.saveAll(pictures);
    List<Long> pictureIds = pictures.stream().map(Picture::getId).toList();
    // when
    List<Picture> expectedPictures = pictureRepository.findByProfileIds(pictureIds);
    // then
    assertThat(expectedPictures.size()).isEqualTo(3);
  }

  @Test
  void deleteProfileByIdsTest() {
    // given
    List<Picture> pictures = createPictures(3);
    pictureRepository.saveAll(pictures);
    List<Long> pictureIds = pictures.stream().map(Picture::getId).toList();
    // when
    pictureRepository.deleteProfileByIds(pictureIds);
    List<Picture> expectedPictures = pictureRepository.findByProfileIds(pictureIds);
    // then
    assertThat(expectedPictures.size()).isEqualTo(0);
  }

  private List<Picture> createPictures(int count) {
    List<Picture> pictures = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
      Picture picture = Picture.builder()
                          .originalFileName(String.format("original %s.jpg", i))
                          .s3Path(String.format("original %s.jpg", i))
                          .build();
      pictures.add(picture);
    }
    return pictures;
  }

}
