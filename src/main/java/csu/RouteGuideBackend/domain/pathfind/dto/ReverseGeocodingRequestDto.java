package csu.RouteGuideBackend.domain.pathfind.dto;

import lombok.Getter;

@Getter
public class ReverseGeocodingRequestDto {
    private Long pathfindId;
    private int index;
    private double lat;
    private double lon;
}
