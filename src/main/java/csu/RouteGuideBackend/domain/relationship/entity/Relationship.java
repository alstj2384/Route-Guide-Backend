package csu.RouteGuideBackend.domain.relationship.entity;


import csu.RouteGuideBackend.domain.member.entity.Member;
import csu.RouteGuideBackend.domain.relationship.RelationshipStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Relationship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "user_email",nullable = false)
    private String userEmail;
    @Column(name = "friend_email",nullable = false)
    private String friendEmail;
    @Column(name = "relationship_status",nullable = false)
    private RelationshipStatus status;
    @Column(name = "is_from",nullable = false)
    private boolean isFrom;

    public void acceptRelationshipRequest(){
        status = RelationshipStatus.ACCEPT;
    }


}
