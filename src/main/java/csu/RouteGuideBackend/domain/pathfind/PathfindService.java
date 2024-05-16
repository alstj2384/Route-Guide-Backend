package csu.RouteGuideBackend.domain.pathfind;

import com.fasterxml.jackson.databind.ObjectMapper;
import csu.RouteGuideBackend.domain.member.MemberRepository;
import csu.RouteGuideBackend.dto.DestinationViewDto;
import csu.RouteGuideBackend.dto.StartPathFindDto;
import csu.RouteGuideBackend.dto.StartPathFindViewDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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

    @Value("${tmap-api-host}")
    private String TMAP_API_HOST;
    @Value("${tmap-api-key}")
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

                JSONObject properties = (JSONObject) obj.get("properties");
                route.setDescription(properties.get("description").toString());

                route.setIndex(Integer.parseInt((properties.get("index")).toString()));
                pathfind.getRoutes().add(route);
                routeRepository.save(route);
            }
        } catch(Exception e){
            return null;
        }

        return new StartPathFindViewDto(save.getId());
    }

}
