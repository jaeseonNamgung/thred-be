package com.thred.datingapp.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class FileNotEmptyValidator implements ConstraintValidator<NotEmptyFile, MultipartFile> {

    @Override
    public void initialize(NotEmptyFile constraintAnnotation) {
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        return file != null && !file.isEmpty();
    }
}
