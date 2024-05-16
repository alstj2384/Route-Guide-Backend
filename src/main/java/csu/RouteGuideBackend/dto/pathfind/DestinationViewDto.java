package csu.RouteGuideBackend.dto.pathfind;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class DestinationViewDto {
    String name;
    String address;
    double x;
    double y;
}
