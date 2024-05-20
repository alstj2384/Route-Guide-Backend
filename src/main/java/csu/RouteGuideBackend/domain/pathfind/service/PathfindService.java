package csu.RouteGuideBackend.domain.pathfind.service;

import csu.RouteGuideBackend.domain.member.entity.Member;
import csu.RouteGuideBackend.domain.member.repository.MemberRepository;
import csu.RouteGuideBackend.domain.parse.dto.PedestrianDto;
import csu.RouteGuideBackend.domain.parse.dto.RouteDto;
import csu.RouteGuideBackend.domain.pathfind.dto.PedestrianResponseDto;
import csu.RouteGuideBackend.domain.pathfind.repository.PathfindRepository;
import csu.RouteGuideBackend.domain.pathfind.repository.RouteRepository;
import csu.RouteGuideBackend.domain.pathfind.dto.RouteRequestDto;
import csu.RouteGuideBackend.domain.pathfind.dto.RouteResponseDto;
import csu.RouteGuideBackend.domain.pathfind.entity.Pathfind;
import csu.RouteGuideBackend.domain.pathfind.entity.Route;
import csu.RouteGuideBackend.domain.pathfind.dto.ReverseGeocodingRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PathfindService {

    private final PathfindRepository pathfindRepository;
    private final RouteRepository routeRepository;
    private final MemberRepository memberRepository;

    public Pathfind findById(Long id){
        return pathfindRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("요청 회원이 존재하지 않습니다"));
    }

    public PedestrianResponseDto updatePedestrian(String email, PedestrianDto dto){
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("요청 회원이 존재하지 않습니다"));
        List<Route> routes = new ArrayList<>();
        Pathfind pathfind = Pathfind.builder()
                .member(member)
                .routes(routes)
                .build();
        Pathfind save = pathfindRepository.save(pathfind);


        for (RouteDto routeDto : dto.getRoute()) {
            Route route = Route.builder()
                    .index(routeDto.getIndex())
                    .description(routeDto.getDescription())
                    .pathfind(pathfind)
                    .x(routeDto.getX())
                    .y(routeDto.getY())
                    .build();
            routes.add(route);
            routeRepository.save(route);
            log.info("{}", route);
        }

        return PedestrianResponseDto.builder()
                .pathfindId(save.getId())
                .build();
    }

    public RouteResponseDto findRoute(RouteRequestDto dto) {
        // 해당 정보 조회
        Pathfind path = pathfindRepository.findById(dto.getPathfindId()).orElseThrow(() -> new IllegalArgumentException("길찾기 요청 정보가 존재하지 않습니다"));

        // Route 개수 조회
        int size = path.getRoutes().size();

        // 해당 번호가 마지막 번호인 경우
        if(dto.getIndex() >= size){
            return RouteResponseDto.builder()
                    .pathfindId(path.getId())
                    .description(null)
                    .index(-1)
                    .remainingDistance(0)
                    .build();
        }

        Route currentRoute = path.getRoutes().get(dto.getIndex());
        // index 정보의 x ,y 값 추출
        double x = currentRoute.getX(); // 경도 Lon
        double y = currentRoute.getY(); // 위도 Lat

        // 현재 위치가 다음 위치와 몇 m 거리인 지 확인
        double distance = haversine(y, x, dto.getY(), dto.getX());
        log.info("{}",distance);


        if(distance <= 3){
            return RouteResponseDto.builder()
                    .pathfindId(path.getId())
                    .description(currentRoute.getDescription())
                    .index(dto.getIndex()+1)
                    .remainingDistance(0)
                    .build();
        } else {
            return RouteResponseDto.builder()
                    .pathfindId(path.getId())
                    .description(null)
                    .index(dto.getIndex())
                    .remainingDistance(distance)
                    .build();
        }
    }

    public String currentLocation(String geocoding, ReverseGeocodingRequestDto dto){
        log.info("currentLocation");
        Pathfind find = pathfindRepository.findById(dto.getPathfindId()).orElseThrow(() -> new IllegalArgumentException("요청 정보가 존재하지 않습니다"));
        List<Route> routes = find.getRoutes();
        Route route = routes.get(dto.getIndex());
        double distance = haversine(route.getY(), route.getX(), dto.getLat(), dto.getLon());

        return info(geocoding, distance);
    }



    private String info(String address, double distance){
        return "현재 위치는 " + address + " 이며, 다음 위치까지" + (int)distance + "미터 남았습니다";
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2){
        // 지구의 반지름(km)
        double R = 6371.0;
        log.info("{},{},{},{}", lat1, lon1, lat2, lon2);
        // 라디안으로 변환
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine 공식 적용
        double a = Math.pow(Math.sin(dLat/2), 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        log.info("{}, {}", a, c);
        // 거리 계산 (km)
        double distance = R * c * 1000;

        return distance;
    }

}
