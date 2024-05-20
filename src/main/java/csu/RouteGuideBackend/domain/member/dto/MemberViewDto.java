package csu.RouteGuideBackend.domain.member.dto;

import csu.RouteGuideBackend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MemberViewDto {
    private int id;
    private String email;
    private String userName;
    private String phoneNumber;
    private int isDisabled;

    public static MemberViewDto toDto(Member entity){
        return MemberViewDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .userName(entity.getUserName())
                .isDisabled(entity.getIsDisabled())
                .build();
    }
}
