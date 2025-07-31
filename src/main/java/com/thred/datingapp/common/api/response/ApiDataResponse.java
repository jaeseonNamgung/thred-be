package com.thred.datingapp.common.api.response;

import com.thred.datingapp.common.error.ErrorCode;
import com.thred.datingapp.common.error.errorCode.BaseErrorCode;
import lombok.Getter;

@Getter
public class ApiDataResponse<T> extends ApiErrorResponse {

  private final T data;

  public ApiDataResponse(T data) {
    super(true, BaseErrorCode.SUCCESS.getHttpStatus().value(), BaseErrorCode.SUCCESS.getHttpStatus().name(), BaseErrorCode.SUCCESS.getMessage());
    this.data = data;
  }

  public ApiDataResponse(T data, String message) {
    super(true, BaseErrorCode.SUCCESS.getHttpStatus().value(), BaseErrorCode.SUCCESS.getHttpStatus().name(), message);
    this.data = data;
  }

  public ApiDataResponse(T data, ErrorCode errorCode) {
    super(false, errorCode.getHttpStatus().value(), errorCode.getHttpStatus().name(), errorCode.getMessage());
    this.data = data;
  }

  public ApiDataResponse(T data, ErrorCode errorCode, String message) {
    super(false, errorCode.getHttpStatus().value(), errorCode.getHttpStatus().name(), message);
    this.data = data;
  }

  public static <T> ApiDataResponse<T> ok(T data) {
    return new ApiDataResponse<>(data);
  }

  public static <T> ApiDataResponse<T> ok(T data, String message) {
    return new ApiDataResponse<>(data, message);
  }

  public static <T> ApiDataResponse<T> error(T data, ErrorCode errorCode) {
    return new ApiDataResponse<>(data, errorCode);
  }
  public static <T> ApiDataResponse<T> error(T data, ErrorCode errorCode, String message) {
    return new ApiDataResponse<>(data, errorCode, message);
  }

}
