package csu.RouteGuideBackend.domain.member.dto;

import csu.RouteGuideBackend.domain.member.Member;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Setter
@ToString
public class MemberJoinDto {

    private String email;
    private String password;
    private String userName;
    private String phoneNumber;
    private int isDisable;

    public static Member toEntity(MemberJoinDto dto, PasswordEncoder passwordEncoder){
        return Member.builder()
                .email(dto.email)
                .userName(dto.userName)
                .password(passwordEncoder.encode(dto.password))
                .phoneNumber(dto.phoneNumber)
                .isDisabled(dto.isDisable)
                .role("ROLE_USER")
                .build();
    }

    public MemberJoinDto(String email, String password, String userName, String phoneNumber, int isDisable) {
        this.email = email;
        this.password = password;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.isDisable = isDisable;
    }
}
