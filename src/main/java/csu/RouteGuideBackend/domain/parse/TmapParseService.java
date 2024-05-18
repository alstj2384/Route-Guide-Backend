package csu.RouteGuideBackend.domain.parse;

import csu.RouteGuideBackend.domain.pathfind.Pathfind;
import csu.RouteGuideBackend.domain.pathfind.Route;
import csu.RouteGuideBackend.domain.tmap.TmapApi;
import csu.RouteGuideBackend.domain.parse.dto.ParseTmapResponse;
import csu.RouteGuideBackend.domain.pathfind.dto.DestinationViewDto;
import csu.RouteGuideBackend.domain.pathfind.dto.StartPathFindViewDto;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TmapParseService {
    // API ENUM에 대한 대표 함수 하나 만들고
    public ParseTmapResponse parseRequest(){
        int a = 0;
        switch(a){
            case TmapApi.DESTINATION:{}~
        }
        TmapApi
    }
    public String parseGeocoding(HttpResponse<String> response) throws ParseException {
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
}
