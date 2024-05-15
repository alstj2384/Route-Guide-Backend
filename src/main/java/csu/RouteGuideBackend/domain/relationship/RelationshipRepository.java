package csu.RouteGuideBackend.domain.relationship;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RelationshipRepository extends JpaRepository<Relationship, Long> {

    Optional<List<Relationship>> findAllByUserEmail(String username);
    Optional<Relationship> findRelationshipByUserEmailAndFriendEmail(String userEmail, String friendEmail);
}
