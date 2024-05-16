package csu.RouteGuideBackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartPathFindDto {
    private double startX;
    private double startY;
    private double endX;
    private double endY;

    private String startName = "%EC%B6%9C%EB%B0%9C";
    private String endName = "%EB%8F%84%EC%B0%A9";
}
