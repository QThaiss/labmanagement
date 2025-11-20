/*
 * @ (#) ExportExcelRequest.java    1.0    23/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.dtos.request;/*
 * @description:
 * @author: Bao Thong
 * @date: 23/10/2025
 * @version: 1.0
 */

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class ExportExcelRequest {

    // Danh sách các ID của TestOrder cần export (tùy chọn)
    private List<String> orderIds;

    // Tên file tùy chỉnh (không bao gồm đuôi .xlsx)
    @Size(max = 100, message = "Tên file tùy chỉnh không được vượt quá 100 ký tự.")
    private String customFileName;

    // *** CÁC TRƯỜNG LỌC THỜI GIAN ***

    // Loại khoảng thời gian: TODAY, THIS_MONTH, THIS_YEAR, ALL_TIME, CUSTOM
    // Nếu không gửi, mặc định sẽ là THIS_MONTH (logic xử lý trong service)
    @Pattern(regexp = "^(TODAY|THIS_MONTH|THIS_YEAR|ALL_TIME|CUSTOM)$",
            message = "Loại khoảng thời gian không hợp lệ. Chỉ chấp nhận TODAY, THIS_MONTH, THIS_YEAR, ALL_TIME, CUSTOM.")
    private String dateRangeType; // Mặc định là THIS_MONTH nếu null

    // Ngày bắt đầu (chỉ bắt buộc nếu dateRangeType = CUSTOM)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // Định dạng YYYY-MM-DD
    private LocalDate startDate;

    // Ngày kết thúc (chỉ bắt buộc nếu dateRangeType = CUSTOM)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // Định dạng YYYY-MM-DD
    private LocalDate endDate;

    // Validation 1: Nếu là CUSTOM thì startDate và endDate phải có giá trị
    @AssertTrue(message = "startDate và endDate là bắt buộc khi dateRangeType là CUSTOM")
    private boolean isCustomDatePresent() {
        if ("CUSTOM".equals(dateRangeType)) {
            // Chỉ kiểm tra sự tồn tại (khác null)
            return startDate != null && endDate != null;
        }
        return true; // Hợp lệ cho các trường hợp khác
    }

    // Validation 2: Nếu là CUSTOM và ngày có giá trị, thì endDate phải sau hoặc bằng startDate
    @AssertTrue(message = "endDate phải sau hoặc trùng với startDate")
    private boolean isDateOrderValid() {
        if ("CUSTOM".equals(dateRangeType) && startDate != null && endDate != null) {
            // Kiểm tra thứ tự ngày
            return !endDate.isBefore(startDate);
        }
        return true; // Hợp lệ nếu không phải CUSTOM hoặc ngày chưa được nhập (để isCustomDatePresent báo lỗi)
    }

    // Validation 3: Nếu không phải CUSTOM thì startDate và endDate phải null
    @AssertTrue(message = "startDate và endDate chỉ được phép khi dateRangeType là CUSTOM")
    private boolean isCustomRangeExclusive() {
        if (!"CUSTOM".equals(dateRangeType)) {
            return startDate == null && endDate == null;
        }
        return true;
    }
}
