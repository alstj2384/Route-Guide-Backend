package csu.RouteGuideBackend.domain.pathfind;

import com.fasterxml.jackson.databind.ObjectMapper;
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


    // 목적지 검색

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
     * tmap api로 받아온 response를 파싱합니다
     * @param response tmap api로 응답된 response body
     * @return List<DestinationViewDto> 목적지 View DTO 리스트
     */
    // 검색 결과 파싱
    public Optional<List<DestinationViewDto>> parseDestination(String response){
        // 응답 정보 파싱
        List<DestinationViewDto> destinationViewList = new ArrayList<>();
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(response);
            JSONObject searchPoiInfo = (JSONObject) json.get("searchPoiInfo");
            JSONObject pois = (JSONObject) searchPoiInfo.get("pois");
            JSONArray poi = (JSONArray) pois.get("poi");

            for (Object o : poi) {
                JSONObject obj = (JSONObject) o;

                JSONObject newAddressList = (JSONObject) obj.get("newAddressList");
                JSONArray newAddress = (JSONArray) newAddressList.get("newAddress");
                JSONObject fullAddress = (JSONObject) newAddress.get(0);

                destinationViewList.add(
                        DestinationViewDto.builder()
                                .name(obj.get("name").toString())
                                .address(fullAddress.get("fullAddressRoad").toString())
                                .x(Double.parseDouble(fullAddress.get("centerLat").toString())) // x
                                .y(Double.parseDouble(fullAddress.get("centerLon").toString())) // y
                                .build());
            }
        } catch(Exception e){
            return Optional.empty();
        }
        return Optional.of(destinationViewList);
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


    /**
     * 길 찾기 시작 api 응답을 파싱합니다
     * @param email - 사용자 email
     * @param response : tmap api로 응답받은 response body
     * @return
     * @throws Exception
     */
    public StartPathFindViewDto parsePath(String email, String response) throws Exception{
        Pathfind pathfind = Pathfind.builder()
                .member(memberRepository.findByEmail(email).orElse(null))
                .routes(new ArrayList<>())
                .build();

        Pathfind save = pathfindRepository.save(pathfind);

        double prevX = 0;
        double prevY = 0;
        int index = 0;

        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(response);
            JSONArray features = (JSONArray) json.get("features");

            for (Object feature : features) {
                JSONObject obj = (JSONObject) feature;

                Route route = new Route();
                route.setPathfind(pathfind);

                JSONObject geometry = (JSONObject) obj.get("geometry");
                JSONArray arr = (JSONArray) geometry.get("coordinates");
                // 단일 좌표인 경우
                if((geometry.get("type")).toString().equals("Point")){
                    route.setX(Double.parseDouble(arr.get(0).toString()));
                    route.setY(Double.parseDouble(arr.get(1).toString()));
                } else{
                    JSONArray coor = (JSONArray) arr.get(0);
                    route.setX(Double.parseDouble(coor.get(0).toString()));
                    route.setY(Double.parseDouble(coor.get(1).toString()));
                }

                // 좌표가 중복으로 제공되는 경우 넘어가기
                if(route.getX() == prevX && route.getY() == prevY) continue;
                prevX = route.getX();
                prevY = route.getY();

                JSONObject properties = (JSONObject) obj.get("properties");
                route.setDescription(properties.get("description").toString());

                route.setIndex(index++);
                pathfind.getRoutes().add(route);
                routeRepository.save(route);
            }
        } catch(Exception e){
            return null;
        }

        return new StartPathFindViewDto(save.getId());
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

    /**
     *
     * @param lat 현재 x위치
     * @param lon 현재 y위치
     * @return ReverseGeocoding api httpresponse
     * @throws Exception
     */

    // lat = 위도, lon = 경도
    public HttpResponse<String> ReverseGeocoding(double lat, double lon) throws Exception{
        log.info("ReverseGeocoding start");
        HttpClient httpClient = HttpClient.newHttpClient();

        // URL 설정
        String uri = TMAP_API_HOST+"/tmap/geo/reversegeocoding?version=1&lat="+lat+"&lon="+lon+"&coordType=WGS84GEO&addressType=A02";
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
        return response;
    }

    public String parseGeocoding(HttpResponse<String> response) throws ParseException{
        log.info("parse Geocoding Response");

        log.info("{}", response.body());

        String body = response.body();
        String address = null;
        // 응답 정보 파싱
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(body);
            log.info("json : {}", json.toString());
            JSONObject addressInfo = (JSONObject) json.get("addressInfo");
            log.info("address info : {}", addressInfo.toString());

            address = (String) addressInfo.get("fullAddress");
        } catch(Exception e){
            throw new ParseException(2);
        }

        return address;
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
