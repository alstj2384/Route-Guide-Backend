package csu.RouteGuideBackend.domain.member.controller;

import csu.RouteGuideBackend.config.PrincipalDetails;
import csu.RouteGuideBackend.domain.member.dto.MemberViewDto;
import csu.RouteGuideBackend.domain.member.service.MemberService;
import csu.RouteGuideBackend.domain.member.dto.MemberEditDto;
import csu.RouteGuideBackend.domain.member.dto.MemberJoinDto;
import csu.RouteGuideBackend.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/member/{email}")
    public ResponseEntity<MemberViewDto> get(@PathVariable String email, @AuthenticationPrincipal PrincipalDetails principalDetails){
        log.info("{} 회원 조회 요청", email);

        // 요청 검증
        valid(principalDetails, email);

        // 멤버 조회
        Member member = memberService.findByEmail(email);

        // DTO 변환
        MemberViewDto dto = MemberViewDto.toDto(member);

        return ResponseEntity.ok().body(dto);
    }

    @PostMapping("/member")
    public ResponseEntity<MemberViewDto> create(@RequestBody MemberJoinDto joinForm){
        log.info("{} 회원 생성 요청", joinForm.toString());
        Member join = memberService.join(joinForm);

        MemberViewDto dto = MemberViewDto.toDto(join);
        return ResponseEntity.ok().body(dto);
    }

    @DeleteMapping("/member/{email}")
    public ResponseEntity<MemberViewDto> delete(@PathVariable String email, @AuthenticationPrincipal PrincipalDetails principalDetails){
        log.info("{} 회원 삭제 요청", email);
        valid(principalDetails, email);

        Member delete = memberService.delete(email);
        MemberViewDto deleted = MemberViewDto.toDto(delete);

        return ResponseEntity.ok().body(deleted);
    }

    @PatchMapping("/member/{email}")
    public ResponseEntity<MemberViewDto> patch(@PathVariable String email, @AuthenticationPrincipal PrincipalDetails principalDetails
            ,@RequestBody MemberEditDto dto ){
        valid(principalDetails, email);

        Member patch = memberService.patch(email, dto);
        MemberViewDto patched = MemberViewDto.toDto(patch);
        return ResponseEntity.ok().body(patched);
    }


    private void valid(PrincipalDetails principalDetails, String email){
        log.info("{} 회원 검증", email);
        if(!principalDetails.getUsername().equals(email)){
            log.info("회원 조회 불가");
            throw new IllegalArgumentException("요청 정보가 토큰과 일치하지 않습니다");
        }
        log.info("회원 검증 완료");
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<String> handleDataAccessException(SQLException ex) {
        // 예외 처리 로직
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> illegalArgumentException(IllegalArgumentException ex) {
        // 예외 처리 로직
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
