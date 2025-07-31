package com.thred.datingapp.inApp.dto.request;

import lombok.Getter;

@Getter
public class GoogleRtdnRequest {

    private Message message;

    @Getter
    public static class Message {
        private String data;
    }
}
