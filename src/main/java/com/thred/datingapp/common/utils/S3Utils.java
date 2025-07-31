package com.thred.datingapp.common.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.ProfileErrorCode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3Utils {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3 amazonS3;

    public String saveImage(MultipartFile multipartFile) {
        if (multipartFile.isEmpty() || Objects.requireNonNull(multipartFile.getOriginalFilename()).isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String uniqueFileName = generateUniqueFileName(originalFilename);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());

        try {
            amazonS3.putObject(bucket, uniqueFileName, multipartFile.getInputStream(), objectMetadata);
            return decodeUrl(amazonS3.getUrl(bucket, uniqueFileName).toString());
        } catch (Exception e) {
            throw new CustomException(ProfileErrorCode.CANT_SAVE_IN_S3);
        }
    }

    public void deleteS3Image(String s3Url) {
        if (s3Url != null) {
            String splitStr = ".com/";
            String fileName = s3Url.substring(s3Url.lastIndexOf(splitStr) + splitStr.length());
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        // png 가져오는 메서드 -> 추후 확장자를 검사할 때 사용
        // String ext=extractExt(originalFileName);
        String uuid = UUID.randomUUID().toString();
        return uuid + "_" + originalFileName;
    }

    private String decodeUrl(String url) {
        return URLDecoder.decode(url, StandardCharsets.UTF_8);
    }
}
