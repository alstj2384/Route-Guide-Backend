package csu.RouteGuideBackend.domain.pathfind;

import csu.RouteGuideBackend.config.PrincipalDetails;
import csu.RouteGuideBackend.dto.pathfind.*;
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

    @GetMapping("/search")
    public ResponseEntity<List<DestinationViewDto>> SearchDestination(@RequestParam("destination") String destination,
                                                                      @RequestParam("x") double x, @RequestParam("y") double y) throws Exception{
        log.info("목적지 검색");
        HttpResponse<String> response = pathfindService.searchDestination(destination, x, y);
        List<DestinationViewDto> destinationViewDtos = pathfindService.parseDestination(response.body()).orElseThrow(() -> new IllegalArgumentException("검색결과가 존재하지 않습니다!"));
        return ResponseEntity.ok().body(destinationViewDtos);
    }

    @PostMapping("/start")
    public ResponseEntity<StartPathFindViewDto> startPathFind(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody StartPathFindDto dto) throws Exception{
        log.info("길찾기 시작 요청");
        HttpResponse<String> response = pathfindService.startPathfind(dto);
        StartPathFindViewDto startPathFindViewDto = pathfindService.parsePath(principalDetails.getUsername(), response.body());
        if(startPathFindViewDto == null){
            throw new Exception("파싱중 오류 발생");
        }

        return ResponseEntity.ok().body(startPathFindViewDto);
    }

    @PostMapping("/route")
    public ResponseEntity<RouteResponseDto> route(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody RouteRequestDto dto){
        log.info("경로 조회 요청");
        Pathfind pathfind = pathfindService.findById(dto.getPathfindId());
        checkValidAndThrowException(principalDetails, pathfind.getMember().getEmail());

        RouteResponseDto response = pathfindService.findRoute(dto);

        return ResponseEntity.ok().body(response);
    }

    private boolean valid(PrincipalDetails principalDetails, String userName){
        return principalDetails.getUsername().equals(userName);
    }

    private void checkValidAndThrowException(PrincipalDetails principalDetails, String userName) throws IllegalArgumentException{
        if(!valid(principalDetails, userName)){
            throw new IllegalArgumentException("요청 정보에 대한 권한이 존재하지 않습니다");
        }
    }

    @PostMapping("/current-location")
    public ResponseEntity<?> reverseGeocoding(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                              @RequestBody GeocodingRequest dto) throws Exception{
        // 경로 정보 조회
        Pathfind pathfind = pathfindService.findById(dto.getPathfindId());
        checkValidAndThrowException(principalDetails, pathfind.getMember().getEmail());

        // 응답 조회
        GeocodingResponse geocodingResponse = pathfindService.ReverseGeocoding(dto);


        // TODO 컨트롤러 부분에서 데이터 처리할 지 서비스에서 한 번에 묶어서 처리할 지 생각해보기

        return ResponseEntity.ok().body(geocodingResponse);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> IlliegalArgumentException(IllegalArgumentException ex) {
        // 예외 처리 로직
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
