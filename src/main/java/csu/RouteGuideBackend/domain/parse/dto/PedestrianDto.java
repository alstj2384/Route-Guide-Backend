package csu.RouteGuideBackend.domain.parse.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class PedestrianDto {
    List<RouteDto> route;
    Long pathfindId;
}
