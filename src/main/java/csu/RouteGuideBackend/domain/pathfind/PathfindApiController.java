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
        RouteResponseDto response = pathfindService.findRoute(dto);

        return ResponseEntity.ok().body(response);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> IlliegalArgumentException(IllegalArgumentException ex) {
        // 예외 처리 로직
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
