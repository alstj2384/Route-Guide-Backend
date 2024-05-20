package csu.RouteGuideBackend.domain.parse.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PedestrianDto {
    List<RouteDto> route;
    Long pathfindId;
}
