package csu.RouteGuideBackend.domain.pathfind.controller;

import csu.RouteGuideBackend.config.PrincipalDetails;
import csu.RouteGuideBackend.domain.parse.TmapParseService;
import csu.RouteGuideBackend.domain.parse.dto.PedestrianDto;
import csu.RouteGuideBackend.domain.parse.dto.PoisResponseDto;
import csu.RouteGuideBackend.domain.pathfind.dto.PedestrianResponseDto;
import csu.RouteGuideBackend.domain.pathfind.dto.RouteRequestDto;
import csu.RouteGuideBackend.domain.pathfind.dto.RouteResponseDto;
import csu.RouteGuideBackend.domain.pathfind.entity.Pathfind;
import csu.RouteGuideBackend.domain.pathfind.service.PathfindService;
import csu.RouteGuideBackend.domain.tmap.TmapApi;
import csu.RouteGuideBackend.domain.tmap.service.TmapRequestService;
import csu.RouteGuideBackend.domain.pathfind.dto.PoisRequestDto;
import csu.RouteGuideBackend.domain.pathfind.dto.PedestrianRequestDto;
import csu.RouteGuideBackend.domain.pathfind.dto.ReverseGeocodingRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.http.HttpResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/path-find")
@Slf4j
public class PathfindApiController {

    private final PathfindService pathfindService;
    private final TmapRequestService tmapRequestService;
    private final TmapParseService tmapParseService;

    @GetMapping("/search")
    public ResponseEntity<PoisResponseDto> pois(@ModelAttribute PoisRequestDto dto) throws ParseException, ResponseStatusException, IllegalArgumentException, IOException, InterruptedException{
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

        return ResponseEntity.ok().body(pois);
    }

    @PostMapping("/start")
    public ResponseEntity<PedestrianResponseDto> pedestrian(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody PedestrianRequestDto dto)
                                                            throws ResponseStatusException, IOException, IllegalArgumentException, InterruptedException, ParseException{

        PedestrianDto pedestrian = null;
        log.info("pedestrian");
        log.info("{}", dto);

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

        // TODO parsing된 내용을 repository에 update
        String email = principalDetails.getUsername();

        PedestrianResponseDto pedestrianResponseDto = pathfindService.updatePedestrian(email, pedestrian);
        // 최종적으로 pathfindId 반환

        return ResponseEntity.ok().body(pedestrianResponseDto);
    }

    @PostMapping("/route")
    public ResponseEntity<RouteResponseDto> route(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody RouteRequestDto dto){
        log.info("경로 조회 요청");
        Pathfind pathfind = pathfindService.findById(dto.getPathfindId());

        // 요청 유효성 검사
        checkValidAndThrowException(principalDetails, pathfind.getMember().getEmail());

        RouteResponseDto response = pathfindService.findRoute(dto);

        return ResponseEntity.ok().body(response);
    }



    @PostMapping("/current-location")
    public ResponseEntity<String> reverseGeocoding(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                @RequestBody ReverseGeocodingRequestDto dto) throws ParseException, ResponseStatusException , IOException, IllegalArgumentException, InterruptedException{

        Pathfind pathfind = pathfindService.findById(dto.getPathfindId());

        // 요청 유효성 검사
        checkValidAndThrowException(principalDetails, pathfind.getMember().getEmail());

        String geocoding = null;
        // 경로 정보 조회
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

        // TODO 응답했던 내용을 DB 기록??

        String info = pathfindService.currentLocation(geocoding, dto);

        return ResponseEntity.ok().body(info);
    }

    private boolean valid(PrincipalDetails principalDetails, String userName){
        return principalDetails.getUsername().equals(userName);
    }

    private void checkValidAndThrowException(PrincipalDetails principalDetails, String userName) throws IllegalArgumentException{
        if(!valid(principalDetails, userName)){
            throw new IllegalArgumentException("요청 정보에 대한 권한이 존재하지 않습니다");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> illiegalArgumentException(IllegalArgumentException ex) {
        // 예외 처리 로직
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<String> parseException(ParseException ex) {
        // 예외 처리 로직
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> responseStatusException(ResponseStatusException ex) {
        // 예외 처리 로직
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> iOException(IOException ex) {
        // 예외 처리 로직
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<String> interruptedException(InterruptedException ex) {
        // 예외 처리 로직
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class})
    public ResponseEntity<String> interruptedException(AuthenticationPrincipal ex) {
        // 예외 처리 로직
        return new ResponseEntity<>("인증이 필요합니다", HttpStatus.BAD_REQUEST);
    }
}
