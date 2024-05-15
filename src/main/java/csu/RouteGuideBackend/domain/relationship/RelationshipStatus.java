package csu.RouteGuideBackend.domain.relationship;

public enum RelationshipStatus {
    ACCEPT(1), WAITING(0);
    private final int value;

    RelationshipStatus(int value) {
        this.value = value;
    }
}
