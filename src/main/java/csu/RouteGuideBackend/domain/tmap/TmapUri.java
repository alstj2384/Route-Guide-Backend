package csu.RouteGuideBackend.domain.tmap;

public enum TmapUri {
    // tmap URI에 따라 종류 나누기
    POIS(0), PEDESTRIAN(1), REVERSE_GEOCODING(2);

    private int value;
    private TmapUri(int value){
        this.value = value;
    }
    public int getValue(){
        return value;
    }

}
