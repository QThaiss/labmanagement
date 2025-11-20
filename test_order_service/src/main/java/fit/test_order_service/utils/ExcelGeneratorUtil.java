/*
 * @ (#) ExcelGeneratorUtil.java    1.0    23/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.utils;/*
 * @description:
 * @author: Bao Thong
 * @date: 23/10/2025
 * @version: 1.0
 */

import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExcelGeneratorUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public ByteArrayOutputStream generateTestOrdersExcel(List<TestOrder> orders, Map<String, String> userIdToNameMap) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Test Orders");

            // --- Tạo Header Row ---
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Id Test Orders", "Patient Name", "Gender", "Date of Birth",
                    "Phone Number", "Status", "Created By", "Created On",
                    "Run By", "Run On"
            };

            // Style cho Header (Optional)
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // --- Tạo Data Rows ---
            int rowIdx = 1;
            CellStyle dateCellStyle = workbook.createCellStyle(); // Style cho ngày tháng
            CreationHelper createHelper = workbook.getCreationHelper();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));

            CellStyle dateTimeCellStyle = workbook.createCellStyle(); // Style cho ngày giờ
            dateTimeCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss"));


            for (TestOrder order : orders) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(order.getOrderId());
                row.createCell(1).setCellValue(order.getFullName());
                row.createCell(2).setCellValue(order.getGender() != null ? order.getGender().name() : "");

                Cell dobCell = row.createCell(3);
                if (order.getDateOfBirth() != null) {
                    dobCell.setCellValue(order.getDateOfBirth());
                    dobCell.setCellStyle(dateCellStyle); // Apply date format
                } else {
                    dobCell.setCellValue("");
                }

                row.createCell(4).setCellValue(order.getPhone());
                row.createCell(5).setCellValue(order.getStatus() != null ? order.getStatus().name() : "");

                // Lấy tên từ map, nếu không có thì trả về ID + lỗi
                row.createCell(6).setCellValue(userIdToNameMap.getOrDefault(order.getCreatedBy(), order.getCreatedBy() + " (Unknown)"));

                Cell createdOnCell = row.createCell(7);
                if (order.getCreatedAt() != null) {
                    createdOnCell.setCellValue(order.getCreatedAt()); // POI tự xử lý LocalDateTime
                    createdOnCell.setCellStyle(dateTimeCellStyle); // Apply date-time format
                } else {
                    createdOnCell.setCellValue("");
                }


                // Run By và Run On chỉ điền nếu status là COMPLETED
                if (order.getStatus() == OrderStatus.COMPLETED) {
                    row.createCell(8).setCellValue(userIdToNameMap.getOrDefault(order.getRunBy(), order.getRunBy() + " (Unknown)"));

                    Cell runOnCell = row.createCell(9);
                    if (order.getRunAt() != null) {
                        runOnCell.setCellValue(order.getRunAt());
                        runOnCell.setCellStyle(dateTimeCellStyle);
                    } else {
                        runOnCell.setCellValue("");
                    }
                } else {
                    row.createCell(8).setCellValue(""); // Để trống
                    row.createCell(9).setCellValue(""); // Để trống
                }
            }

            // --- Tự động điều chỉnh độ rộng cột ---
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            log.info("Excel content generated successfully for {} orders.", orders.size());
            return out;

        } catch (IOException e) {
            log.error("Error generating Excel content: {}", e.getMessage(), e);
            return null; // Trả về null nếu có lỗi
        }
    }
}
