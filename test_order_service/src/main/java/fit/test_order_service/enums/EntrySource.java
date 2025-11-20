package fit.test_order_service.enums;

public enum EntrySource {
    HL7, MANUAL, IMPORT,
    MANUAL_WEB,       // Được tạo thủ công bởi bác sĩ/y tá trên web
    AUTO_INSTRUMENT // Được tạo tự động bởi hệ thống instrument khi nhận mẫu từ thiết bị
}
