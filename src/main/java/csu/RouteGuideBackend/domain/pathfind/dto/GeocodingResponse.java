package csu.RouteGuideBackend.domain.pathfind.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeocodingResponse {
    private String description;
}
