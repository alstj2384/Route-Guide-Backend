package csu.RouteGuideBackend.domain.parse;

import csu.RouteGuideBackend.domain.parse.dto.PedestrianDto;
import csu.RouteGuideBackend.domain.parse.dto.PoisResponseDto;
import csu.RouteGuideBackend.domain.parse.dto.RouteDto;
import csu.RouteGuideBackend.domain.tmap.TmapApi;
import csu.RouteGuideBackend.domain.parse.dto.PoisDto;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TmapParseService {
    // API ENUM에 대한 대표 함수 하나 만들고
    public Object parseRequest(TmapApi api, String response) throws Exception{
        Object result = null;
        switch(api){
            case POIS :
                result = pois(response);
                break;
            case PEDESTRIAN :
                result = pedestrian(response);
                break;
            case REVERSE_GEOCODING:
                result = geocoding(response);
                break;
            default:
                throw new IllegalArgumentException("올바르지 않은 enum type입니다");
        }
        return result;
    }

    private String geocoding(String response) throws ParseException {
        log.info("parse Geocoding");

        String address = null;
        // 응답 정보 파싱
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(response);
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
     * 길 찾기 시작 api 응답을 파싱합니다
     * @param email - 사용자 email
     * @param response : tmap api로 응답받은 response body
     * @return
     * @throws Exception
     */
    private PedestrianDto pedestrian(String response) throws ParseException{
        log.info("parse pedestrian");

//        Pathfind pathfind = Pathfind.builder()
//                .member(memberRepository.findByEmail(email).orElse(null))
//                .routes(new ArrayList<>())
//                .build();
//
//        Pathfind save = pathfindRepository.save(pathfind);

        PedestrianDto pedestrianDto = new PedestrianDto();
        List<RouteDto> route = new ArrayList<>();

        pedestrianDto.setRoute(route);
        double prevX = 0;
        double prevY = 0;
        double x = 0;
        double y = 0;
        int index = 0;

        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(response);
            JSONArray features = (JSONArray) json.get("features");

            for (Object feature : features) {
                JSONObject obj = (JSONObject) feature;

                RouteDto dto = new RouteDto();

//                Route route = new Route();
//                route.setPathfind(pathfind);

                JSONObject geometry = (JSONObject) obj.get("geometry");
                JSONArray arr = (JSONArray) geometry.get("coordinates");
                // 단일 좌표인 경우
                if((geometry.get("type")).toString().equals("Point")){

                    x = Double.parseDouble(arr.get(0).toString());
                    y = Double.parseDouble(arr.get(1).toString());
                } else{
                    JSONArray coor = (JSONArray) arr.get(0);
                    x = Double.parseDouble(coor.get(0).toString());
                    y = Double.parseDouble(coor.get(1).toString());
                }

                // 좌표가 중복으로 제공되는 경우 넘어가기
                if(x == prevX && y == prevY) continue;
                prevX = x;
                prevY = y;

                dto.setX(x);
                dto.setY(y);

                JSONObject properties = (JSONObject) obj.get("properties");
                dto.setDescription(properties.get("description").toString());

                dto.setIndex(index++);
                route.add(dto);
//                routeRepository.save(route);
            }
        } catch(Exception e){
            throw new ParseException(2);
        }

        return pedestrianDto;
    }

    /**
     * tmap api로 받아온 response를 파싱합니다
     * @param response tmap api로 응답된 response body
     * @return List<DestinationViewDto> 목적지 View DTO 리스트
     */
    // 검색 결과 파싱
    private PoisResponseDto pois(String response) throws ParseException{
        log.info("parse pois");
        // 응답 정보 파싱
        PoisResponseDto poisResponseDto = new PoisResponseDto();
        List<PoisDto> poisDto = new ArrayList<>();

        poisResponseDto.setPois(poisDto);
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

                poisDto.add(
                        PoisDto.builder()
                                .name(obj.get("name").toString())
                                .address(fullAddress.get("fullAddressRoad").toString())
                                .x(Double.parseDouble(fullAddress.get("centerLat").toString())) // x
                                .y(Double.parseDouble(fullAddress.get("centerLon").toString())) // y
                                .build());
            }
        } catch(Exception e){
            throw new ParseException(2);
        }
        return poisResponseDto;
    }
}
