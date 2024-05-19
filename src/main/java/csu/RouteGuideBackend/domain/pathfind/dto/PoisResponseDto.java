package csu.RouteGuideBackend.domain.pathfind.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class PoisResponseDto {
    String name;
    String address;
    double x;
    double y;
}
