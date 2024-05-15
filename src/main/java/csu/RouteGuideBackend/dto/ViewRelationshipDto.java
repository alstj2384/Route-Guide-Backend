package csu.RouteGuideBackend.dto;

import csu.RouteGuideBackend.domain.relationship.RelationshipStatus;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
@Builder
@Getter
@ToString
public class ViewRelationshipDto {
    private Long relationshipId;
    private String relationEmail;
    private String relationName;
    private RelationshipStatus status;
}
