package csu.RouteGuideBackend.domain.parse.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class PoisDto {
    String name;
    String address;
    double x;
    double y;
}
