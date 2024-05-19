package csu.RouteGuideBackend.domain.tmap.dto;

import lombok.Getter;

@Getter
public class PoisRequestDto {
    private String destination;
    private double x;
    private double y;
}
