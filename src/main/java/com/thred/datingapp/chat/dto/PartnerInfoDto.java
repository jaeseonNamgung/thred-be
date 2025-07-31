package com.thred.datingapp.chat.dto;

import com.thred.datingapp.common.entity.chat.ChatPart;

public record PartnerInfoDto(
    Long id,
    String nickName,
    String mainProfile
) {

  public static PartnerInfoDto toDto(ChatPart chatPart){
    return new PartnerInfoDto(
            chatPart.getUser().getId(),
            chatPart.getUser().getUsername(),
            chatPart.getUser().getMainProfile());
  }
}
