package fit.test_order_service.utils;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import fit.test_order_service.client.IamFeignClient;
import fit.test_order_service.client.dtos.UserInternalResponse;
import fit.test_order_service.dtos.response.ApiResponse;
// --- SỬA 1: IMPORT DTO MỚI ---
import fit.test_order_service.dtos.response.CommentOrderResponse;
// import fit.test_order_service.entities.OrderComment; // (Bỏ entity cũ)
import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.entities.TestResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PdfGeneratorUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final IamFeignClient iamFeignClient;

    private static final String FONT_REGULAR = "fonts/times.ttf";
    private static final String FONT_BOLD = "fonts/timesbd.ttf";


    // --- SỬA 2: THAY ĐỔI CHỮ KÝ PHƯƠNG THỨC ĐỂ NHẬN DTO ---
    public byte[] generateTestResultPdf(TestOrder order, List<TestResult> results, List<CommentOrderResponse> comments) {
        if (order == null) {
            log.error("Cannot generate PDF: TestOrder object is null.");
            return null;
        }
        log.debug("Generating PDF for Order ID: {}", order.getOrderId());
        log.debug("Received {} results and {} comments.", (results != null ? results.size() : 0), (comments != null ? comments.size() : 0));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] finalBytes;

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc, PageSize.A4)) {

            PdfFont fontRegular;
            PdfFont fontBold;

            try {
                fontRegular = PdfFontFactory.createFont(
                        FONT_REGULAR, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
                );
                fontBold = PdfFontFactory.createFont(
                        FONT_BOLD, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
                );
                log.debug("Đã load font tiếng Việt (Regular, Bold) cho document này.");
            } catch (IOException e) {
                log.warn("Không thể load font 'fonts/times.ttf'. Sử dụng Helvetica.", e);
                // Fallback
                fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            }

            document.setFont(fontRegular);
            log.debug("Set default font. BAOS size: {}", baos.size());

            // --- Tiêu đề ---
            try {
                document.add(new Paragraph("Test Order Results")
                        .setFont(fontBold)
                        .setFontSize(18)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(15));
                document.add(new Paragraph("Order Code: " + getValueOrNA(order.getOrderCode())).setFontSize(10));
                document.add(new Paragraph("Patient Name: " + getValueOrNA(order.getFullName())).setFontSize(10));
                document.add(new Paragraph("Date of Birth: " + (order.getDateOfBirth() != null ? order.getDateOfBirth().format(DATE_FORMATTER) : "N/A")).setFontSize(10));
                document.add(new Paragraph("Gender: " + (order.getGender() != null ? order.getGender().name() : "N/A")).setFontSize(10));
                document.add(new Paragraph("Phone: " + getValueOrNA(order.getPhone())).setFontSize(10));
                document.add(new Paragraph("\n").setFontSize(10));
                log.debug("Added Header info successfully. BAOS size: {}", baos.size());
            } catch (Exception e) {
                log.error("Error adding Header info: {}", e.getMessage(), e);
                throw e;
            }

            // --- Bảng 1: Thông tin Test Order ---
            try {
                document.add(new Paragraph("Order Information").setFont(fontBold).setFontSize(12).setMarginBottom(5));
                Table orderInfoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth();
                orderInfoTable.setFontSize(9);

                addRow(orderInfoTable, "Order ID", order.getOrderId(), fontRegular, fontBold);
                addRow(orderInfoTable, "Patient Name", order.getFullName(), fontRegular, fontBold);
                addRow(orderInfoTable, "Gender", order.getGender() != null ? order.getGender().name() : "N/A", fontRegular, fontBold);
                addRow(orderInfoTable, "Date of Birth", order.getDateOfBirth() != null ? order.getDateOfBirth().format(DATE_FORMATTER) : "N/A", fontRegular, fontBold);
                addRow(orderInfoTable, "Phone Number", order.getPhone(), fontRegular, fontBold);
                addRow(orderInfoTable, "Status", order.getStatus() != null ? order.getStatus().name() : "N/A", fontRegular, fontBold);
                addRow(orderInfoTable, "Created By", getUserFullName(order.getCreatedBy()), fontRegular, fontBold);
                addRow(orderInfoTable, "Created On", order.getCreatedAt() != null ? order.getCreatedAt().format(DATETIME_FORMATTER) : "N/A", fontRegular, fontBold);
                addRow(orderInfoTable, "Run By", getUserFullName(order.getRunBy()), fontRegular, fontBold);
                addRow(orderInfoTable, "Run On", order.getRunAt() != null ? order.getRunAt().format(DATETIME_FORMATTER) : "N/A", fontRegular, fontBold);

                document.add(orderInfoTable);
                log.debug("Added Table 1 successfully. BAOS size: {}", baos.size());
            } catch (Exception e) {
                log.error("Error adding Table 1: {}", e.getMessage(), e);
                throw e;
            }

            document.add(new Paragraph("\n").setFontSize(10));

            // --- Bảng 2: Kết quả Xét nghiệm ---
            try {
                document.add(new Paragraph("Test Results").setFont(fontBold).setFontSize(12).setMarginBottom(5));
                if (results != null && !results.isEmpty()) {
                    Table resultsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1.5f, 1, 2, 1, 2})).useAllAvailableWidth();
                    resultsTable.setFontSize(9);

                    resultsTable.addHeaderCell(createHeaderCell("Analyte Name", fontBold));
                    resultsTable.addHeaderCell(createHeaderCell("Value", fontBold));
                    resultsTable.addHeaderCell(createHeaderCell("Unit", fontBold));
                    resultsTable.addHeaderCell(createHeaderCell("Reference Range", fontBold));
                    resultsTable.addHeaderCell(createHeaderCell("Flag", fontBold));
                    resultsTable.addHeaderCell(createHeaderCell("Measured At", fontBold));

                    for (TestResult result : results) {
                        resultsTable.addCell(createCell(result.getAnalyteName(), false, fontRegular, fontBold));
                        resultsTable.addCell(createCell(result.getValueText(), false, fontRegular, fontBold));
                        resultsTable.addCell(createCell(result.getUnit(), false, fontRegular, fontBold));
                        resultsTable.addCell(createCell(result.getReferenceRange(), false, fontRegular, fontBold));
                        resultsTable.addCell(createCell(result.getAbnormalFlag() != null ? result.getAbnormalFlag().name() : null, false, fontRegular, fontBold));
                        resultsTable.addCell(createCell(result.getMeasuredAt() != null ? result.getMeasuredAt().format(DATETIME_FORMATTER) : null, false, fontRegular, fontBold));
                    }
                    document.add(resultsTable);
                    log.debug("Added Table 2 successfully with {} results. BAOS size: {}", results.size(), baos.size());
                } else {
                    document.add(new Paragraph("No test results available.").setFontSize(10));
                    log.debug("Added 'No results' message. BAOS size: {}", baos.size());
                }
            } catch (Exception e) {
                log.error("Error adding Table 2: {}", e.getMessage(), e);
                throw e;
            }

            document.add(new Paragraph("\n").setFontSize(10));

            // --- SỬA 3: CẬP NHẬT LOGIC IN COMMENT ĐỂ DÙNG DTO ---
            try {
                document.add(new Paragraph("Comments").setFont(fontBold).setFontSize(12).setMarginBottom(5));
                if (comments != null && !comments.isEmpty()) {

                    // Gọi hàm helper đệ quy để in
                    addCommentsToDocument(document, comments, 0, fontRegular, fontBold);

                    log.debug("Added Comments successfully with {} top-level comments. BAOS size: {}", comments.size(), baos.size());
                } else {
                    document.add(new Paragraph("No comments found.").setFontSize(10));
                    log.debug("Added 'No comments' message. BAOS size: {}", baos.size());
                }
            } catch (Exception e) {
                log.error("Error adding Comments: {}", e.getMessage(), e);
                throw e;
            }

            log.info("All content added successfully. Before closing document. BAOS size: {}", baos.size());

        } catch (IOException e) {
            log.error("IO Error during PDF generation for order {}: {}", order.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("IO Error during PDF generation", e);
        } catch (Exception e) {
            log.error("General Error during PDF generation for order {}: {}", order.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("General Error during PDF generation", e);
        }

        finalBytes = baos.toByteArray();
        log.info("PDF generation process completed for order {}. Final byte array size: {} bytes", order.getOrderId(), finalBytes.length);
        return finalBytes;
    }

    // --- SỬA 4: HÀM ĐỆ QUY MỚI ĐỂ IN COMMENT VÀ REPLIES ---
    private void addCommentsToDocument(Document document, List<CommentOrderResponse> comments, int level, PdfFont fontRegular, PdfFont fontBold) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        float indent = level * 20f; // Thụt lề 20f cho mỗi cấp độ reply

        for (CommentOrderResponse comment : comments) {
            String authorName = "N/A";
            if (comment.getAuthor() != null) {
                authorName = getValueOrNA(comment.getAuthor().getFullName());
            }

            Paragraph commentPara = new Paragraph()
                    .setFont(fontRegular)
                    .setMarginLeft(indent) // Áp dụng thụt lề
                    .add((comment.getCreatedAt() != null ? comment.getCreatedAt().format(DATETIME_FORMATTER) : "[No Date]"))
                    .add(" - [")
                    .add(new Paragraph(authorName).setFont(fontBold).setBold()) // In đậm tên
                    .add("]: ")
                    .add(getValueOrNA(comment.getContent()))
                    .setFontSize(9)
                    .setMarginBottom(3);

            // In thông tin target (nếu là comment về kết quả)
            if (comment.getTargetInfo() != null && comment.getTargetInfo().getTargetType() == fit.test_order_service.enums.CommentTargetType.RESULT) {
                Paragraph targetPara = new Paragraph(
                        String.format("(Về kết quả: %s - %s)",
                                getValueOrNA(comment.getTargetInfo().getTestName()),
                                getValueOrNA(comment.getTargetInfo().getAnalyteName()))
                )
                        .setFont(fontRegular)
                        .setFontSize(8)
                        .setItalic()
                        .setMarginLeft(indent + 10f) // Thụt lề thêm
                        .setMarginBottom(3);
                document.add(targetPara);
            }

            document.add(commentPara);

            // Gọi đệ quy cho replies
            addCommentsToDocument(document, comment.getReplies(), level + 1, fontRegular, fontBold);
        }
    }


    // --- Helper Methods (Không thay đổi) ---

    private Cell createCell(String content, boolean isBold, PdfFont fontRegular, PdfFont fontBold) {
        Paragraph paragraph = new Paragraph(getValueOrNA(content));
        if (isBold) {
            paragraph.setFont(fontBold);
        } else {
            paragraph.setFont(fontRegular);
        }
        return new Cell().add(paragraph).setTextAlignment(TextAlignment.LEFT);
    }

    private Cell createHeaderCell(String content, PdfFont fontBold) {
        Paragraph paragraph = new Paragraph(content).setFont(fontBold);
        return new Cell().add(paragraph).setTextAlignment(TextAlignment.CENTER);
    }

    private void addRow(Table table, String key, String value, PdfFont fontRegular, PdfFont fontBold) {
        table.addCell(createCell(key, true, fontRegular, fontBold));
        table.addCell(createCell(value, false, fontRegular, fontBold));
    }

    private String getValueOrNA(String value) {
        return (value != null && !value.isBlank()) ? value : "N/A";
    }

    // (Hàm này giờ chỉ dùng cho Bảng 1, không dùng cho comment nữa)
    private String getUserFullName(String userId) {
        if (userId == null || userId.isBlank() || "SYSTEM".equals(userId)) {
            return userId != null ? userId : "N/A";
        }
        try {
            log.debug("Calling IAM service to get full name for user ID: {}", userId);
            ApiResponse<UserInternalResponse> response = iamFeignClient.getUserById(userId);
            if (response != null && response.getData() != null && response.getData().fullName() != null) {
                log.debug("Successfully retrieved full name for user ID {}: {}", userId, response.getData().fullName());
                return response.getData().fullName();
            } else {
                log.warn("Could not retrieve full name for user ID: {}. Response or data was null or fullName was null.", userId);
                return userId + " (Name not found)";
            }
        } catch (Exception e) {
            log.error("Error calling IAM service for user ID {}: {}", userId, e.getMessage(), e);
            return userId + " (Error retrieving name)";
        }
    }
}