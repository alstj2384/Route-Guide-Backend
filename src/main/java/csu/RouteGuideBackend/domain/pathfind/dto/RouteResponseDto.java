package csu.RouteGuideBackend.domain.pathfind.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RouteResponseDto {
    private Long pathfindId;
    private int index;
    private double remainingDistance;
    private String description;

    public RouteResponseDto(Long pathfindId, int index, double remainingDistance, String description) {
        this.pathfindId = pathfindId;
        this.index = index;
        this.remainingDistance = remainingDistance;
        this.description = description;
    }
}
