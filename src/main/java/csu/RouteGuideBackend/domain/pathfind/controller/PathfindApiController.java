package csu.RouteGuideBackend.domain.pathfind.controller;

import csu.RouteGuideBackend.config.PrincipalDetails;
import csu.RouteGuideBackend.domain.parse.TmapParseService;
import csu.RouteGuideBackend.domain.parse.dto.PedestrianDto;
import csu.RouteGuideBackend.domain.parse.dto.PoisDto;
import csu.RouteGuideBackend.domain.parse.dto.PoisResponseDto;
import csu.RouteGuideBackend.domain.pathfind.service.PathfindService;
import csu.RouteGuideBackend.domain.tmap.TmapApi;
import csu.RouteGuideBackend.domain.tmap.service.TmapRequestService;
import csu.RouteGuideBackend.domain.tmap.dto.PoisRequestDto;
import csu.RouteGuideBackend.domain.tmap.dto.PedestrianRequestDto;
import csu.RouteGuideBackend.domain.tmap.dto.ReverseGeocodingRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/path-find")
@Slf4j
public class PathfindApiController {

    private final PathfindService pathfindService;
    private final TmapRequestService tmapRequestService;
    private final TmapParseService tmapParseService;

    @GetMapping("/search")
    public PoisResponseDto pois(@ModelAttribute PoisRequestDto dto) throws Exception{
//    public ResponseEntity<List<DestinationViewDto>> pois(@RequestParam PoisRequestDto dto) throws Exception{
        log.info("pois");
        PoisResponseDto pois = null;

        // 목적지 검색 API 요청
        HttpResponse<String> response = tmapRequestService.pois(dto);
        log.info("pois HttpResponse : {}", response);

        // 응답 내용 파싱
        Object parse = tmapParseService.parseRequest(TmapApi.POIS, response.body());

        // 타입 유효성 검사
        if(parse instanceof PoisResponseDto) {
            pois = (PoisResponseDto) parse;
            log.info("pois parseResponse : {}", parse);
        }

        return pois;
    }

    @PostMapping("/start")
//    public ResponseEntity<pedestrianResponseDto> pedestrian(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody PedestrianRequestDto dto) throws Exception{
    public PedestrianDto pedestrian(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody PedestrianRequestDto dto) throws Exception{
        PedestrianDto pedestrian = null;
        log.info("pedestrian");

        // 보행자 길찾기 API 요청
        HttpResponse<String> response = tmapRequestService.pedestrian(dto);
        log.info("pedestrian HttpResponse : {}", response);

        // 응답 내용 파싱
        Object parse = tmapParseService.parseRequest(TmapApi.PEDESTRIAN, response.body());

        // 리턴 타입 검사
        if(parse instanceof PedestrianDto){
            pedestrian = (PedestrianDto) parse;
            log.info("pedestrian parseResponse : {}", parse);
        }
        return pedestrian;
    }
//
//    @PostMapping("/route")
//    public ResponseEntity<RouteResponseDto> route(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody RouteRequestDto dto){
//    public RouteResponseDto route(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody RouteRequestDto dto){
//        log.info("경로 조회 요청");
//        Pathfind pathfind = pathfindService.findById(dto.getPathfindId());
//        checkValidAndThrowException(principalDetails, pathfind.getMember().getEmail());
//
//        RouteResponseDto response = pathfindService.findRoute(dto);
//
//        return ResponseEntity.ok().body(response);
//    }
//
//    private boolean valid(PrincipalDetails principalDetails, String userName){
//        return principalDetails.getUsername().equals(userName);
//    }
//
//    private void checkValidAndThrowException(PrincipalDetails principalDetails, String userName) throws IllegalArgumentException{
//        if(!valid(principalDetails, userName)){
//            throw new IllegalArgumentException("요청 정보에 대한 권한이 존재하지 않습니다");
//        }
//    }
//
    @PostMapping("/current-location")
//    public ResponseEntity<?> reverseGeocoding(@AuthenticationPrincipal PrincipalDetails principalDetails,
//                                              @RequestBody ReverseGeocodingRequestDto dto) throws Exception{
    public String reverseGeocoding(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                @RequestBody ReverseGeocodingRequestDto dto) throws Exception{
        String geocoding = null;
        // 경로 정보 조회
//        Pathfind pathfind = pathfindService.findById(dto.getPathfindId());
//        checkValidAndThrowException(principalDetails, pathfind.getMember().getEmail());
        log.info("reverseGeocoding");
        // 좌표 정보로 현재 위치 조회 API 요청
        HttpResponse<String> response = tmapRequestService.reverseGeocoding(dto);
        log.info("reverseGeocoding HttpResponse : {}", response);

        // 응답 정보 파싱
        Object parse = tmapParseService.parseRequest(TmapApi.REVERSE_GEOCODING, response.body());

        // 타입 유효성 검사
        if(parse instanceof String) {
            geocoding = (String) parse;
            log.info("reverseGeocoding parseResponse : {}", parse);
        }


        return geocoding;
//         응답 조회
//        GeocodingResponse geocodingResponse = pathfindService.ReverseGeocoding(dto);
//
//
//         TODO 컨트롤러 부분에서 데이터 처리할 지 서비스에서 한 번에 묶어서 처리할 지 생각해보기
//
//        return ResponseEntity.ok().body(geocodingResponse);
//        return null;
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> IlliegalArgumentException(IllegalArgumentException ex) {
        // 예외 처리 로직
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
