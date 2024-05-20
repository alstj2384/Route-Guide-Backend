package csu.RouteGuideBackend.domain.member.controller;

import csu.RouteGuideBackend.config.PrincipalDetails;
import csu.RouteGuideBackend.domain.member.service.MemberService;
import csu.RouteGuideBackend.domain.member.dto.MemberEditDto;
import csu.RouteGuideBackend.domain.member.dto.MemberJoinDto;
import csu.RouteGuideBackend.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/member/{id}")
    public ResponseEntity<Member> get(@PathVariable int id, @AuthenticationPrincipal PrincipalDetails principalDetails){
        log.info("id {}번 회원 검색", id);
        if(isValidMember(principalDetails, id)){
            return ResponseEntity.ok().body(memberService.findById(id));
        }
        log.info("회원을 찾을 수 없습니다!");
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping("/member")
    public ResponseEntity<Member> create(@RequestBody MemberJoinDto joinForm){
        log.info("{}", joinForm.toString());
        Member join = memberService.join(joinForm);

        return ResponseEntity.ok().body(join);
    }

    @DeleteMapping("/member/{id}")
    public ResponseEntity<Member> delete(@PathVariable int id, @AuthenticationPrincipal PrincipalDetails principalDetails){
        if(isValidMember(principalDetails, id)){
            return ResponseEntity.ok(memberService.delete(id));
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PatchMapping("/member/{id}")
    public ResponseEntity<Member> patch(@PathVariable int id, @AuthenticationPrincipal PrincipalDetails principalDetails
            ,@RequestBody MemberEditDto dto ){
        if(isValidMember(principalDetails, id)){
            Member patch = memberService.patch(id, dto);
            return ResponseEntity.ok(patch);
        }
        return ResponseEntity.badRequest().body(null);
    }


    private boolean isValidMember(PrincipalDetails principalDetails, int id){
        log.info("id {} 회원 검증", id);
        return memberService.findByEmail(principalDetails.getUsername()).getId() == id;
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<String> handleDataAccessException(SQLException ex) {
        // 예외 처리 로직
        return new ResponseEntity<>("데이터베이스 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
