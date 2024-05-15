package csu.RouteGuideBackend.domain.member;

import csu.RouteGuideBackend.domain.relationship.Relationship;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Entity
@Getter
@Setter
@DynamicUpdate
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "is_disabled", nullable = false)
    private int isDisabled;

    @Column(name = "role", nullable = false)
    private String role;


    @Column(name = "provider", nullable = true)
    private String provider;

    @Column(name = "provider_id", nullable = true)
    private String providerId;

    @OneToMany(mappedBy = "member", fetch = FetchType.EAGER)
    private List<Relationship> relationshipList = new ArrayList<>();

    @Builder
    public Member(int id, String email, String password, String userName, String phoneNumber, int isDisabled, String role, String provider, String providerId, List<Relationship> relationshipList) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.isDisabled = isDisabled;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
        this.relationshipList = relationshipList;
    }
}
