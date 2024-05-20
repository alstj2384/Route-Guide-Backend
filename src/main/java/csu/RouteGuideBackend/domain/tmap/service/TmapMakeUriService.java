package csu.RouteGuideBackend.domain.tmap.service;

import csu.RouteGuideBackend.domain.tmap.TmapUri;
import csu.RouteGuideBackend.domain.pathfind.dto.PoisRequestDto;
import csu.RouteGuideBackend.domain.pathfind.dto.ReverseGeocodingRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
@Slf4j
public class TmapMakeUriService {
    @Value("${tmap.api.host}")
    private String TMAP_API_HOST;

    public String getUri(TmapUri type, Object dto) throws UnsupportedEncodingException, IllegalArgumentException{
        String uri = null;
        log.info("TmapMakeUriService 호출");
        switch(type){
            case POIS :
                uri = makePoisUri(dto); break;
            case PEDESTRIAN :
                uri = makePedestrian(dto); break;
            case REVERSE_GEOCODING :
                uri = makeReverseGeocoding(dto); break;
            default :
                throw new IllegalArgumentException("올바르지 않은 enum type입니다");
        }
        return uri;
    }

    private String makePedestrian(Object dto) {
        log.info("makePedestrian 호출");
        return TMAP_API_HOST+"/tmap/routes/pedestrian?version=1";
    }

    private String makeReverseGeocoding(Object dto) {
        ReverseGeocodingRequestDto request = (ReverseGeocodingRequestDto) dto;
        log.info("makeReverseGeocoding 호출");

        return TMAP_API_HOST+"/tmap/geo/reversegeocoding?version=1&lat="+request.getLat()+"&lon="+request.getLon()+"&coordType=WGS84GEO&addressType=A02";
    }

    private String makePoisUri(Object dto) throws UnsupportedEncodingException{
        PoisRequestDto request = (PoisRequestDto) dto;
        log.info("makePoisUri 호출");

        return TMAP_API_HOST+"/tmap/pois?version=1&searchKeyword="+ URLEncoder.encode(request.getDestination(), "utf-8") +
                "&searchType=all&page=1&count=10" +
                "&reqCoordType=WGS84GEO&resCoordType=WGS84GEO&searchtypCd=R" +
                "&radius=5&centerLat="+request.getX()+"&centerLon="+request.getY()+
                "&multiPoint=N&poiGroupYn=N";
    }
}
