package csu.RouteGuideBackend.domain.pathfind;

import com.fasterxml.jackson.databind.ObjectMapper;
import csu.RouteGuideBackend.domain.member.Member;
import csu.RouteGuideBackend.domain.member.MemberRepository;
import csu.RouteGuideBackend.dto.pathfind.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PathfindService {

    @Value("${tmap.api.host}")
    private String TMAP_API_HOST;
    @Value("${tmap.api.key}")
    private String TMAP_API_KEY;

    private final PathfindRepository pathfindRepository;
    private final RouteRepository routeRepository;
    private final MemberRepository memberRepository;

    public Pathfind findById(Long id){
        return pathfindRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("요청 회원이 존재하지 않습니다"));
    }

    /**
     * 현재 위치와 다음 경로의 좌표를 비교하여 다음 경로를 안내합니다
     * @param dto
     * @return
     */
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



    public String info(String address, double distance){
        return "현재 위치는 " + address + " 이며, 다음 위치까지" + (int)distance + "미터 남았습니다";
    }

    /**
     * 두 좌표를 바탕으로 거리를 m로 반환합니다
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    double haversine(double lat1, double lon1, double lat2, double lon2){
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
