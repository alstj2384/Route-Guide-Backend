package csu.RouteGuideBackend.domain.tmap;

public enum TmapApi {
    //TODO 각 요청 API 에 따른 ENUM 이름과 값 정리
    POIS(0), PEDESTRIAN(1), REVERSE_GEOCODING(2);

    private int value;
    private TmapApi(int value){
        this.value = value;
    }
    public int getValue(){
        return value;
    }
}
