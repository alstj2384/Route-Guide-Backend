package csu.RouteGuideBackend.dto.pathfind;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class RouteRequestDto {
    private Long pathfindId;
    private int index;
    private double x;
    private double y;

}
