package csu.RouteGuideBackend.domain.pathfind.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PedestrianRequestDto {
    private double startX;
    private double startY;
    private double endX;
    private double endY;

    private String startName = "%EC%B6%9C%EB%B0%9C";
    private String endName = "%EB%8F%84%EC%B0%A9";
}
