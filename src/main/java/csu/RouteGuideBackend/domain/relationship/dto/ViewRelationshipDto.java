package csu.RouteGuideBackend.domain.relationship.dto;

import csu.RouteGuideBackend.domain.relationship.RelationshipStatus;
import lombok.*;

@Data
@Builder
@Getter
@ToString
@RequiredArgsConstructor
public class ViewRelationshipDto {
    private Long relationshipId;
    private String relationEmail;
    private String relationName;
    private RelationshipStatus status;

    public ViewRelationshipDto(Long relationshipId, String relationEmail, String relationName, RelationshipStatus status) {
        this.relationshipId = relationshipId;
        this.relationEmail = relationEmail;
        this.relationName = relationName;
        this.status = status;
    }

}
