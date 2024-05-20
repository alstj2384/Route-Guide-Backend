package csu.RouteGuideBackend.domain.member.service;

import csu.RouteGuideBackend.domain.member.dto.MemberEditDto;
import csu.RouteGuideBackend.domain.member.dto.MemberJoinDto;
import csu.RouteGuideBackend.domain.member.entity.Member;
import csu.RouteGuideBackend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public Member join(MemberJoinDto dto){
        Member entity = MemberJoinDto.toEntity(dto, passwordEncoder);

        Optional<Member> member = memberRepository.findByEmail(entity.getEmail());

        if(member.isPresent())
            throw new IllegalArgumentException("해당 이메일 사용자가 이미 존재합니다");

        return memberRepository.save(entity);
    }


    public Member findByEmail(String email){
        return memberRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("findByEmail : 회원 조회 실패"));
    }

    public Member findById(int id) {
        return memberRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("findById : 회원 조회 실패"));
    }

    public Member delete(String email) {
        Member target = findByEmail(email);
        memberRepository.delete(target);
        return target;
    }


    public Member patch(String email, MemberEditDto dto) {
        Member target = findByEmail(email);
        Member entity = MemberEditDto.toEntity(target, dto, passwordEncoder);
        memberRepository.save(entity);
        return entity;
    }
}
