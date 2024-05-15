package csu.RouteGuideBackend.domain.relationship;

import csu.RouteGuideBackend.config.PrincipalDetails;
import csu.RouteGuideBackend.domain.member.Member;
import csu.RouteGuideBackend.domain.member.MemberService;
import csu.RouteGuideBackend.dto.ViewRelationshipDto;
import csu.RouteGuideBackend.dto.WaitingRelationshipDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member/{email}/relationship")
@RequiredArgsConstructor
@Slf4j
public class RelationshipApiController {

    private final MemberService memberService;
    private final RelationshipService relationshipService;

    /**
     * 사용자 - 보호자 관계 공통
     */

    // 친구 목록 조회
    @GetMapping()
    public ResponseEntity<List<ViewRelationshipDto>> getRelationList(@AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable("email") String email) throws Exception{
        isValid(principalDetails, email);
        log.info("친구 목록 조회");
        List<ViewRelationshipDto> relationList = relationshipService.getRelationList(principalDetails.getUsername());

        return ResponseEntity.ok().body(relationList);
    }

    // 처리되지 않은 확인하지 않은 요청 리스트
    @GetMapping("/request")
    public ResponseEntity<List<WaitingRelationshipDto>> getWaitingRelationshipList(@AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable("email") String email) throws Exception{
        isValid(principalDetails, email);
        log.info("요청 목록 조회");
        List<WaitingRelationshipDto> waitingRelationshipList = relationshipService.getWaitingRelationshipList(principalDetails.getUsername());
        return ResponseEntity.ok().body(waitingRelationshipList);
    }

    // 요청 취소
    @DeleteMapping("/{relationshipId}")
    public ResponseEntity<ViewRelationshipDto> deleteRelationship(@AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable("email") String email, @PathVariable("relationshipId") Long relationshipId) throws Exception{
        isValid(principalDetails, email);
        log.info("삭제 요청 완료");
        Relationship relationship = relationshipService.rejectRelationshipRequest(relationshipId);

        Member friend = memberService.findByEmail(relationship.getFriendEmail());
        ViewRelationshipDto dto = ViewRelationshipDto.builder()
                .relationshipId(relationship.getId())
                .relationName(friend.getUserName())
                .relationEmail(friend.getEmail())
                .status(relationship.getStatus())
                .build();
        return ResponseEntity.ok().body(dto);
    }


    /**
     * 보호자 - 사용자 관계
     */
    // 친구 추가 요청 수락
    @PostMapping("/request/{relationshipId}/approve")
    public ResponseEntity<String> approveRelationship(@AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable("relationshipId") Long id,  @PathVariable("email") String email) throws Exception{
        isValid(principalDetails, email);
        log.info("{} 수락 요청", id);
        Relationship relationship = relationshipService.approveRelationshipRequest(id);
        return ResponseEntity.ok().body("요청 수락 성공");
    }


    /**
     * 사용자 - 보호자 관계
     */
    @PostMapping("/request/{counterpartEmail}")
    public ResponseEntity<String> sendRelationshipRequest(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                          @PathVariable("email") String email, @PathVariable("counterpartEmail") String counterpartEmail) throws Exception{

        isValid(principalDetails, email);

        // 자기 자신을 조회하는 경우
        if(counterpartEmail.equals(email)){
            log.info("자기 자신을 조회했습니다") ;
            throw new IllegalArgumentException("회원 조회 실패");
        }
        memberService.findByEmail(email);
        memberService.findByEmail(counterpartEmail);

        log.info("중복 회원 검증");
        relationshipService.isAlreadyExist(email, counterpartEmail);
        relationshipService.createRelationship(email, counterpartEmail);

        log.info("친구 추가 성공");
        return ResponseEntity.ok("친구 추가 요청 성공");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> userNameNotFoundException(UsernameNotFoundException ex) {
        // 예외 처리 로직
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> IlliegalArgumentException(IllegalArgumentException ex) {
        // 예외 처리 로직
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    private boolean isValid(PrincipalDetails principalDetails, String email){
        log.info("{}, {}", principalDetails.getUsername(), email) ;
        if(principalDetails.getUsername().equals(email)){
            log.info("토큰이 정보와 일치합니다");
            return true;
        }
        log.info("토큰이 정보와 일치하지 않습니다");
        throw new IllegalArgumentException("토큰 정보와 요청 정보가 일치하지 않습니다");
    }
}
