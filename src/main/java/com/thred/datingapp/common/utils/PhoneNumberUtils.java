package com.thred.datingapp.common.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PhoneNumberUtils {

  public static String toE164Format(String phoneNumber) {
    try {
      PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
      Phonenumber.PhoneNumber parsedPhoneNumber = phoneNumberUtil.parse(phoneNumber, "KR");
      return phoneNumberUtil.format(parsedPhoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
    } catch (NumberParseException e) {
      log.error("[toE164Format] 잘못된 전화번호 형식입니다. ===> phoneNumber: {}", phoneNumber);
      throw new CustomException(UserErrorCode.PHONE_NUMBER_PARSE_ERROR, e);
    }
  }

  public static String toLocalFormat(String phoneNumber) {
    try {
      PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
      Phonenumber.PhoneNumber parsedPhoneNumber = phoneNumberUtil.parse(phoneNumber, "KR");
      return phoneNumberUtil.format(parsedPhoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
    } catch (NumberParseException e) {
      log.error("[toLocalFormat] 잘못된 전화번호 형식입니다. ===> phoneNumber: {}", phoneNumber);
      throw new CustomException(UserErrorCode.PHONE_NUMBER_PARSE_ERROR, e);
    }
  }
}
