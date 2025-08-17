package com.thred.datingapp.user.validation;

import com.thred.datingapp.common.annotation.MinFileCount;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class MinFileCountValidator implements ConstraintValidator<MinFileCount, List<MultipartFile>> {

    private int min;

    @Override
    public void initialize(MinFileCount constraintAnnotation) {
        this.min = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext constraintValidatorContext) {
        if (multipartFiles == null) {
            return false;
        }
        return multipartFiles.size() >= min;
    }
}
