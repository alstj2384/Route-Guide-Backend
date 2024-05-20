package csu.RouteGuideBackend.domain.pathfind.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class PoisRequestDto {
    private String destination;
    private double x;
    private double y;

}
