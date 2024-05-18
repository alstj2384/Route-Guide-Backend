package csu.RouteGuideBackend.domain.relationship;

import csu.RouteGuideBackend.domain.member.Member;
import csu.RouteGuideBackend.domain.member.MemberRepository;
import csu.RouteGuideBackend.domain.relationship.dto.ViewRelationshipDto;
import csu.RouteGuideBackend.domain.relationship.dto.WaitingRelationshipDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class RelationshipService {
    private final MemberRepository memberRepository;
    private final RelationshipRepository relationshipRepository;


    /**
     * Relationship 요청을 생성합니다
     * @param toEmail 요청을 보내는 사용자의 Email
     * @param fromEmail 요청을 받는 사용자의 Email
     * @return void
     * @throws UsernameNotFoundException
     */
    public void createRelationship(String fromEmail, String toEmail) throws Exception {

        // 두 회원의 Member 객체 조회
        Member fromMember = memberRepository.findByEmail(fromEmail).orElseThrow(() -> new UsernameNotFoundException("회원 조회 실패"));
        Member toMember = memberRepository.findByEmail(toEmail).orElseThrow(() -> new UsernameNotFoundException("회원 조회 실패"));

        log.info("from Member = {}", fromMember);
        log.info("to Member = {}", toMember);

        // 보내는 사람의 Relationship 객체 생성
        Relationship relationshipFrom = Relationship.builder()
                .member(fromMember)
                .userEmail(fromEmail)
                .friendEmail(toEmail)
                .status(RelationshipStatus.WAITING)
                .isFrom(true)
                .build();

        // 받는 사람의 Relationship 객체 생성
        Relationship relationshipTo = Relationship.builder()
                .member(toMember)
                .userEmail(toEmail)
                .friendEmail(fromEmail)
                .status(RelationshipStatus.WAITING)
                .isFrom(false)
                .build();

        log.info("from relationshipFrom = {}", relationshipFrom);
        log.info("from relationshipTo = {}", relationshipTo);

        // 각자 회원의 RelationshipList에 Relationship 추가
        fromMember.getRelationshipList().add(relationshipTo);
        toMember.getRelationshipList().add(relationshipFrom);

        // DB에 저장
        relationshipRepository.save(relationshipTo);
        relationshipRepository.save(relationshipFrom);

    }


    /**
     * 처리되지 않은 요청을 조회합니다
     * @param userName 목록을 조회할 userName
     * @return List<WaitingRelationshipDto>
     * @throws UsernameNotFoundException
     */
    @Transactional
    public List<WaitingRelationshipDto> getWaitingRelationshipList(String userName) throws Exception{
        // 요청 목록을 읽어들일 사용자의 Member 객체를 조회한다
        Member member = memberRepository.findByEmail(userName).orElseThrow(() -> new UsernameNotFoundException("회원 조회 실패"));

        // Member 객체에서 RelationshipList를 조회한다
        List<Relationship> relationshipList = member.getRelationshipList();

        List<WaitingRelationshipDto> result = new ArrayList<>();

        for (Relationship x : relationshipList) {
            if(!x.isFrom() && x.getStatus() == RelationshipStatus.WAITING){
                Member friend = memberRepository.findByEmail(x.getFriendEmail()).orElseThrow(() -> new UsernameNotFoundException("회원 조회 실패"));
                WaitingRelationshipDto dto = WaitingRelationshipDto.builder()
                        .relationshipId(x.getId())
                        .relationEmail(friend.getEmail())
                        .relationName(friend.getUserName())
                        .status(x.getStatus())
                        .build();
                result.add(dto);
            }
        }
        return result;
    }


    /**
     * 처리되지 않은 요청을 수락합니다
     * @param id 요청을 수락할 RelationshipId
     * @return 수락한 Relationship 객체
     * @throws UsernameNotFoundException
     */
    public Relationship approveRelationshipRequest(Long id) throws Exception{
        Relationship relationship = relationshipRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("요청을 찾을 수 없습니다!"));
        Relationship friendRelationship = relationshipRepository.findRelationshipByUserEmailAndFriendEmail(relationship.getMember().getEmail(), relationship.getFriendEmail())
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다"));


        relationship.acceptRelationshipRequest();
        friendRelationship.acceptRelationshipRequest();

        relationshipRepository.save(relationship);
        relationshipRepository.save(friendRelationship);
        return relationship;
    }

    /**
     * 처리되지 않은 요청을 거절합니다
     * @param id 요청을 거절할 RelationshipId
     * @return 거절한 Relationship 객체
     * @throws UsernameNotFoundException
     */
    public Relationship rejectRelationshipRequest(Long id) throws Exception{
        Relationship relationship = relationshipRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("요청을 찾을 수 없습니다!"));
        Relationship friendRelationship = relationshipRepository.findRelationshipByUserEmailAndFriendEmail(relationship.getFriendEmail(),relationship.getMember().getEmail())
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다"));

        relationshipRepository.delete(relationship);
        relationshipRepository.delete(friendRelationship);

        return relationship;
    }

    //
    /**
     * ACCEPT 된 Relationship를 조회합니다
     * @param userName 목록을 조회할 userName
     * @return 조회된 List<Relationship>
     * @throws UsernameNotFoundException
     */
    public List<ViewRelationshipDto> getRelationList(String userName) throws Exception{
        List<ViewRelationshipDto> result = new ArrayList<>();
        List<Relationship> list = relationshipRepository.findAllByUserEmail(userName).orElseThrow(() -> new UsernameNotFoundException("회원 조회 실패"));
        for (Relationship x : list) {
            if(x.getStatus() == RelationshipStatus.ACCEPT) {
                Member friend = memberRepository.findByEmail(x.getFriendEmail()).orElseThrow(() -> new UsernameNotFoundException("회원 조회 실패"));
                ViewRelationshipDto dto = ViewRelationshipDto.builder()
                        .relationshipId(x.getId())
                        .relationEmail(friend.getEmail())
                        .relationName(friend.getUserName())
                        .status(x.getStatus())
                        .build();
                result.add(dto);
            }
        }
        return result;
    }


    /**
     * Relationship 요청 중복을 검사합니다
     * @param friendEmail 상대방 Email
     * @param userEmail 요청자 Email
     * @throws IllegalArgumentException
     */
    public void isAlreadyExist(String userEmail, String friendEmail){
        if(relationshipRepository.findRelationshipByUserEmailAndFriendEmail(userEmail, friendEmail)
                .isPresent())
            throw new IllegalArgumentException("이미 추가된 친구입니다");
    }








}
