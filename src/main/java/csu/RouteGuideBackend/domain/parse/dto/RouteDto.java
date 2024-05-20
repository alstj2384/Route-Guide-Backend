package csu.RouteGuideBackend.domain.parse.dto;

import csu.RouteGuideBackend.domain.pathfind.entity.Pathfind;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class RouteDto {
    private double x;
    private double y;
    private String description;
    private int index;
}
