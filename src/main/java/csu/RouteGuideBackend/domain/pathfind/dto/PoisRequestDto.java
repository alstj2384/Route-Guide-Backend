package csu.RouteGuideBackend.domain.pathfind.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PoisRequestDto {
    private String destination;
    private double lon;
    private double lat;

}
