package csu.RouteGuideBackend.domain.relationship;


import csu.RouteGuideBackend.domain.member.Member;
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

    @Column(name = "counterpart_id")
    private Long counterpartId;

    public void acceptRelationshipRequest(){
        status = RelationshipStatus.ACCEPT;
    }

    public void setCounterpartId(Long id){
        counterpartId = id;
    }

}
