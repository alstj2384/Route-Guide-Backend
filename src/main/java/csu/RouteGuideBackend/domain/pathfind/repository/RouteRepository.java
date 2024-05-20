package csu.RouteGuideBackend.domain.pathfind.repository;

import csu.RouteGuideBackend.domain.pathfind.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
}
