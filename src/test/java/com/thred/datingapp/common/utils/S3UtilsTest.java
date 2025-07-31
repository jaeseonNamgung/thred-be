package com.thred.datingapp.common.utils;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.when;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.ProfileErrorCode;
import com.thred.datingapp.user.repository.PictureRepository;
import jakarta.persistence.EntityManager;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("testCode")
@Transactional
class S3UtilsTest {
    @Autowired
    S3Utils s3Utils;
    @MockBean
    AmazonS3          amazonS3;
    @Autowired
    EntityManager     em;

    @Test
    @DisplayName("saveImage - 사진이 null이 들어오면 null을 리턴한다.")
    void saveImage_null() {
        // given
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );
        // when
        String url = s3Utils.saveImage(multipartFile);
        // then
        assertThat(url).isNull();
    }

    @Test
    @DisplayName("saveImage - 정상적인 사진이 들어오면 Picture 객체를 리턴한다.")
    void saveImage_success() throws MalformedURLException {
        // given
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "profile.png",
                "image/png",
                new byte[1]
        );
        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(new URL("https://example.com/aaa"));
        // when
        String url = s3Utils.saveImage(multipartFile);
        // then
        assertThat(url).isEqualTo("https://example.com/aaa");
    }

    @Test
    @DisplayName("saveImage - s3 저장때 예외가 터지면 커스텀 예외가 터진다.")
    void saveImage_exception() {
        // given
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "profile.png",
                "image/png",
                new byte[1]
        );
        when(amazonS3.putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class))).thenThrow(
                AmazonServiceException.class);
        // when & then
        assertThatThrownBy(() -> s3Utils.saveImage(multipartFile))
                .isInstanceOf(CustomException.class)
                .hasMessage(ProfileErrorCode.CANT_SAVE_IN_S3.getMessage());
    }

    @Test
    @DisplayName("deleteS3Image - 파일 이름을 적절히 파싱하여 s3 삭제 요청을 한다.")
    void deleteS3Image_success() {
        // given
        String file = "https://www.aws.com/a.png";
        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        // when
        s3Utils.deleteS3Image(file);
        // then
        verify(amazonS3, times(1)).deleteObject(captor.capture());
        DeleteObjectRequest value = captor.getValue();
        assertThat(value.getKey()).isEqualTo("a.png");
    }


}
