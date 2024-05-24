package csu.RouteGuideBackend.domain.tmap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import csu.RouteGuideBackend.domain.tmap.TmapUri;
import csu.RouteGuideBackend.domain.pathfind.dto.PoisRequestDto;
import csu.RouteGuideBackend.domain.pathfind.dto.ReverseGeocodingRequestDto;
import csu.RouteGuideBackend.domain.pathfind.dto.PedestrianRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class TmapRequestService {
    @Value("${tmap.api.key}")
    private String TMAP_API_KEY;
    private final TmapMakeUriService tmapMakeUriService;

    // TODO 이 부분에서 httpclient와 objectMapper를 하나로 관리해도 될까? -> 요청이 여러군데에서 온다면??
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();


    // https://tmap-skopenapi.readme.io/reference/%EC%9E%A5%EC%86%8C%ED%86%B5%ED%95%A9%EA%B2%80%EC%83%89
    public HttpResponse<String> pois(PoisRequestDto dto) throws ResponseStatusException, IllegalArgumentException, IOException, InterruptedException{
        log.info("pois 호출");

        // URI 작성
        String uri = tmapMakeUriService.getUri(TmapUri.POIS, dto);
        log.info("request uri : {}", uri);

        // Request 헤더 작성
        HttpRequest request = buildGetHttpRequest(uri);
        log.info("request : {}", request);

        // Request 전송 및 응답 저장
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("response : {}", response);

        // 응답 오류여부 체크
        checkError(response);

        return response;
    }


    // https://tmap-skopenapi.readme.io/reference/%EB%B3%B4%ED%96%89%EC%9E%90-%EA%B2%BD%EB%A1%9C%EC%95%88%EB%82%B4
    public HttpResponse<String> pedestrian(PedestrianRequestDto dto) throws ResponseStatusException, IOException, IllegalArgumentException, InterruptedException {
        log.info("pedestrian 호출");


        // URI 설정
        String uri = tmapMakeUriService.getUri(TmapUri.PEDESTRIAN, dto);
        log.info("request uri : {}", uri);


        // Request 헤더 및 바디 작성
        String requestBody = objectMapper.writeValueAsString(dto);
        HttpRequest request = buildPostHttpRequest(uri, requestBody);
        log.info("request : {}", request);

        // Request 전송 및 응답 저장
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("response : {}", response);

        // 응답 오류여부 체크
        checkError(response);

        return response;
    }


    // lat = 위도, lon = 경도
    // https://tmap-skopenapi.readme.io/reference/reversegeocoding
    public HttpResponse<String> reverseGeocoding(ReverseGeocodingRequestDto dto) throws ResponseStatusException, IOException, IllegalArgumentException, InterruptedException {
        log.info("reverseGeocoding 호출");

        // URI 설정
        String uri = tmapMakeUriService.getUri(TmapUri.REVERSE_GEOCODING, dto);
        log.info("request uri : {}", uri);

        // Request 헤더 작성
        HttpRequest request = buildGetHttpRequest(uri);
        log.info("request : {}", request);

        // Request 전송 및 응답 저장
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("response : {}", response);


        // 응답 오류여부 체크
        checkError(response);
        return response;

    }


    private void checkError(HttpResponse<String> response) throws ResponseStatusException{

        if(response.statusCode() == 401 || response.statusCode() == 400) {
            log.info("checkError : 권한 없음 예외 발생");

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, response.body());
        }
        log.info("checkError : no Error");
    }

    private HttpRequest buildGetHttpRequest(String uri){
        return HttpRequest.newBuilder(URI.create(uri))
                .GET()
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("appKey", TMAP_API_KEY)
                .build();
    }

    private HttpRequest buildPostHttpRequest(String uri, String requestBody){
        return HttpRequest.newBuilder(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("appKey", TMAP_API_KEY)
                .build();
    }
}
