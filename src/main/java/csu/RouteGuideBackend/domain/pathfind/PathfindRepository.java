package csu.RouteGuideBackend.domain.pathfind;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PathfindRepository extends JpaRepository<Pathfind, Long> {


}
