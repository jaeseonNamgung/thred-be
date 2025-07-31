package com.thred.datingapp.main.dto.response;

public record AnswerResponse(
        String question,
        String answer
) {
    public static AnswerResponse of(String question, String answer) {
        return new AnswerResponse(question, answer);
    }
    public static AnswerResponse of(String question) {
        return new AnswerResponse(question, "");
    }
}
