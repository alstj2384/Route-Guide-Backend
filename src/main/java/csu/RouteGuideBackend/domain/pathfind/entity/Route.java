package csu.RouteGuideBackend.domain.pathfind.entity;

import csu.RouteGuideBackend.domain.pathfind.entity.Pathfind;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Route {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathfind_id", nullable = false)
    private Pathfind pathfind;

    @Column(name = "x", nullable = false)
    private double x;
    @Column(name = "y", nullable = false)
    private double y;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "index", nullable = false)
    private int index;

    @Override
    public String toString() {
        return "Route{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", description='" + description + '\'' +
                ", index=" + index +
                '}';
    }

}
