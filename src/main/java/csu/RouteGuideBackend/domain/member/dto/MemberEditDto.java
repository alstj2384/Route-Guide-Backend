package csu.RouteGuideBackend.domain.member.dto;

import csu.RouteGuideBackend.domain.member.Member;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Setter
public class MemberEditDto {


    private String password;
    private String userName;
    private String phoneNumber;


    public static Member toEntity(Member target, MemberEditDto dto, PasswordEncoder passwordEncoder){
        if(dto.getPassword() != null){
            target.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if(dto.getUserName() != null){
            target.setUserName(dto.userName);
        }
        if(dto.getPhoneNumber() != null){
            target.setPhoneNumber(dto.phoneNumber);
        }
        return target;
    }



}
