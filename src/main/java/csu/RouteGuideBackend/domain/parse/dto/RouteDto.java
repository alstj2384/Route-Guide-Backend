package csu.RouteGuideBackend.domain.parse.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class RouteDto {
    private double lon;
    private double lat;
    private String description;
    private int index;
}
