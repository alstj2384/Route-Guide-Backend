package csu.RouteGuideBackend.domain.pathfind.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RouteRequestDto {
    private Long pathfindId;
    private int index;
    private double x;
    private double y;

}
