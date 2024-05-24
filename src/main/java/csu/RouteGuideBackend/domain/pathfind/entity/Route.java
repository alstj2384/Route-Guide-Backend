package csu.RouteGuideBackend.domain.pathfind.entity;

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

    @Column(name = "lon", nullable = false)
    private double lon;
    @Column(name = "lat", nullable = false)
    private double lat;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "index", nullable = false)
    private int index;

    @Override
    public String toString() {
        return "Route{" +
                "id=" + id +
                ", x=" + lon +
                ", y=" + lat +
                ", description='" + description + '\'' +
                ", index=" + index +
                '}';
    }

}
