package csu.RouteGuideBackend.domain.tmap.dto;

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