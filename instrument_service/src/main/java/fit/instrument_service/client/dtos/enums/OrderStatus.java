package fit.instrument_service.client.dtos.enums;

public enum OrderStatus {
    PENDING, CANCELLED, COMPLETED, IN_PROGRESS;

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case PENDING -> target == COMPLETED || target == CANCELLED || target == IN_PROGRESS;
            case IN_PROGRESS -> target == COMPLETED || target == CANCELLED;
            case COMPLETED -> false;
            case CANCELLED -> false;
        };
    }
}
