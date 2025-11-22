package fit.test_order_service.enums;

public enum OrderStatus {
    PENDING, CANCELLED, COMPLETED, IN_PROGRESS, FAILED,
    AUTO_CREATED,           // Đơn tự tạo, chưa đủ thông tin
    PENDING_PATIENT_MATCH   // Chờ gán bệnh nhân & test type
}
