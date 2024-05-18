package csu.RouteGuideBackend.domain.tmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import csu.RouteGuideBackend.domain.pathfind.Pathfind;
import csu.RouteGuideBackend.domain.pathfind.Route;
import csu.RouteGuideBackend.domain.pathfind.dto.GeocodingRequest;
import csu.RouteGuideBackend.domain.pathfind.dto.GeocodingResponse;
import csu.RouteGuideBackend.domain.pathfind.dto.StartPathFindDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class TmapRequestService {
    @Value("${tmap.api.host}")
    private String TMAP_API_HOST;
    @Value("${tmap.api.key}")
    private String TMAP_API_KEY;

    /**
     * 검색어로 연관된 10개의 목적지를 검색합니다
     * @param destination : 목적지 이름
     * @param x : 사용자의 현재 x 좌표 위치
     * @param y : 사용자의 현재 y 좌표 위치
     * @return
     * @throws Exception
     */

    public HttpResponse<String> searchDestination(String destination, double x, double y) throws Exception{
        log.info("목적지 검색 시작");
        HttpClient httpClient = HttpClient.newHttpClient();

        // URL 설정
        String uri = TMAP_API_HOST+"/tmap/pois?version=1&searchKeyword="+ URLEncoder.encode(destination, "utf-8") +
                "&searchType=all&page=1&count=10" +
                "&reqCoordType=WGS84GEO&resCoordType=WGS84GEO&searchtypCd=R" +
                "&radius=5&centerLat="+x+"&centerLon="+y+
                "&multiPoint=N&poiGroupYn=N";


        // Request 헤더 작성
        log.info("request uri : {}", uri);
        HttpRequest request = HttpRequest.newBuilder(URI.create(uri))
                .GET()
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("appKey", TMAP_API_KEY)
                .build();

        // Request 전송 및 응답 저장
        log.info("request : {}", request);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() == 401){
            throw new IllegalArgumentException("권한이 없습니다!");
        }
        return response;
    }

    /**
     * 길 찾기를 시작합니다
     * @param dto
     * @return - tmap api 응답
     * @throws Exception
     */
    public HttpResponse<String> startPathfind(StartPathFindDto dto) throws Exception{
        log.info("길찾기 경로 탐색 시작");
        HttpClient httpClient = HttpClient.newHttpClient();

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(dto);
        // URL 설정
        String uri = TMAP_API_HOST+"/tmap/routes/pedestrian?version=1";

        // Request 헤더 작성
        HttpRequest request = HttpRequest.newBuilder(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("appKey", TMAP_API_KEY)
                .build();

        // Request 전송 및 응답 저장
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // 잘 받은 거 확인
        // 이제 파싱하고, 데이터 저장하면 됨
        log.info("{}", response.body());

        return response;
    }

    // lat = 위도, lon = 경도
    public GeocodingResponse ReverseGeocoding(GeocodingRequest dto) throws Exception{
        log.info("ReverseGeocoding start");
        HttpClient httpClient = HttpClient.newHttpClient();

        // URL 설정
        String uri = TMAP_API_HOST+"/tmap/geo/reversegeocoding?version=1&lat="+dto.getLat()+"&lon="+dto.getLon()+"&coordType=WGS84GEO&addressType=A02";
        log.info("request uri : {}", uri);


        // Request 헤더 작성
        HttpRequest request = HttpRequest.newBuilder(URI.create(uri))
                .GET()
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("appKey", TMAP_API_KEY)
                .build();

        // Request 전송 및 응답 저장
        log.info("request : {}", request);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() == 401){
            throw new IllegalArgumentException("권한이 없습니다!");
        }

        // 응답 정보 파싱
        String address = parseGeocoding(response);

        // 거리 계산을 위한 객체 받아오기
        Pathfind find = pathfindRepository.findById(dto.getPathfindId()).orElseThrow(() -> new IllegalArgumentException("요청 정보가 존재하지 않습니다"));
        Route route = find.getRoutes().get(dto.getIndex());

        // 거리 계산
        double distance = haversine(route.getY(), route.getX(), dto.getLat(), dto.getLon());

        return GeocodingResponse.builder().description(info(address, distance)).build();
    }
}
