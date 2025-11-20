package fit.test_order_service.enums;

public enum ItemStatus {
    PENDING, CANCELLED, COMPLETED, IN_PROGRESS;

    public boolean canTransitionTo(ItemStatus target) {
        return switch (this) {
            case PENDING -> target == COMPLETED || target == CANCELLED || target == IN_PROGRESS;
            case IN_PROGRESS -> target == COMPLETED || target == CANCELLED;
            case COMPLETED -> false;
            case CANCELLED -> false;
        };
    }
}
