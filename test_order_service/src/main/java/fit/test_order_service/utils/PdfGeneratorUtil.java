package fit.test_order_service.utils;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import fit.test_order_service.client.IamFeignClient;
import fit.test_order_service.client.dtos.UserInternalResponse;
import fit.test_order_service.dtos.response.ApiResponse;
import fit.test_order_service.dtos.response.CommentOrderResponse;
import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.entities.TestResult;
import fit.test_order_service.enums.AbnormalFlag;
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
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IamFeignClient iamFeignClient;

    private static final String FONT_REGULAR = "fonts/times.ttf";
    private static final String FONT_BOLD = "fonts/timesbd.ttf";

    // --- COLORS CONFIGURATION ---
    private static final Color PRIMARY_COLOR = new DeviceRgb(0, 51, 102); // Xanh y tế
    private static final Color HEADER_BG_COLOR = new DeviceRgb(240, 240, 240); // Xám nhạt cho header bảng
    private static final Color ALERT_COLOR = ColorConstants.RED; // Đỏ cho cảnh báo

    public byte[] generateTestResultPdf(TestOrder order, List<TestResult> results, List<CommentOrderResponse> comments) {
        if (order == null) {
            log.error("Cannot generate PDF: TestOrder object is null.");
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc, PageSize.A4)) {

            // 1. Load Font (Có fallback)
            PdfFont fontRegular = loadFont(FONT_REGULAR, StandardFonts.HELVETICA);
            PdfFont fontBold = loadFont(FONT_BOLD, StandardFonts.HELVETICA_BOLD);

            document.setFont(fontRegular);
            document.setMargins(30, 30, 30, 30);

            // 2. HEADER PHÒNG KHÁM (Thêm vào cho chuyên nghiệp)
            addClinicHeader(document, fontBold);

            // Đường kẻ phân cách
            LineSeparator ls = new LineSeparator(new SolidLine(1f));
            ls.setMarginTop(10).setMarginBottom(15);
            document.add(ls);

            // 3. TIÊU ĐỀ PHIẾU
            document.add(new Paragraph("PHIẾU KẾT QUẢ XÉT NGHIỆM")
                    .setFont(fontBold)
                    .setFontSize(16)
                    .setFontColor(PRIMARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15));

            // 4. BẢNG 1: THÔNG TIN ORDER (Giữ nguyên nội dung field, chỉ format đẹp hơn)
            try {
                document.add(new Paragraph("I. THÔNG TIN CHUNG (ORDER INFORMATION)")
                        .setFont(fontBold).setFontSize(11).setFontColor(PRIMARY_COLOR).setMarginBottom(5));

                // Dùng bảng 4 cột để bố trí gọn hơn (Label - Value | Label - Value) nhưng vẫn giữ đủ data
                // Nếu bạn muốn giữ y nguyên dạng danh sách dọc 2 cột như cũ thì đổi float[]{1, 2}
                Table orderTable = new Table(UnitValue.createPercentArray(new float[]{1.5f, 3.5f, 1.5f, 3.5f})).useAllAvailableWidth();
                orderTable.setFontSize(9);
                orderTable.setMarginBottom(15);

                // Row 1
                addStyledRow(orderTable, "Order ID", order.getOrderId(), fontBold, fontRegular);
                addStyledRow(orderTable, "Patient Name", getValueOrNA(order.getFullName()).toUpperCase(), fontBold, fontRegular);

                // Row 2
                addStyledRow(orderTable, "Gender", order.getGender() != null ? order.getGender().name() : "N/A", fontBold, fontRegular);
                addStyledRow(orderTable, "Date of Birth", order.getDateOfBirth() != null ? order.getDateOfBirth().format(DATE_FORMATTER) : "N/A", fontBold, fontRegular);

                // Row 3
                addStyledRow(orderTable, "Phone Number", getValueOrNA(order.getPhone()), fontBold, fontRegular);
                addStyledRow(orderTable, "Status", order.getStatus() != null ? order.getStatus().name() : "N/A", fontBold, fontRegular);

                // Row 4
                addStyledRow(orderTable, "Created By", getUserFullName(order.getCreatedBy()), fontBold, fontRegular);
                addStyledRow(orderTable, "Created On", order.getCreatedAt() != null ? order.getCreatedAt().format(DATETIME_FORMATTER) : "N/A", fontBold, fontRegular);

                // Row 5
                addStyledRow(orderTable, "Run By", getUserFullName(order.getRunBy()), fontBold, fontRegular);
                addStyledRow(orderTable, "Run On", order.getRunAt() != null ? order.getRunAt().format(DATETIME_FORMATTER) : "N/A", fontBold, fontRegular);

                document.add(orderTable);
            } catch (Exception e) {
                log.error("Error adding Table 1", e);
                throw e;
            }

            // 5. BẢNG 2: KẾT QUẢ XÉT NGHIỆM (Format lại: Header màu, Zebra stripe, Highlight bất thường)
            try {
                document.add(new Paragraph("II. KẾT QUẢ (TEST RESULTS)")
                        .setFont(fontBold).setFontSize(11).setFontColor(PRIMARY_COLOR).setMarginBottom(5));

                if (results != null && !results.isEmpty()) {
                    Table resultsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1.5f, 1, 2, 1, 2})).useAllAvailableWidth();
                    resultsTable.setFontSize(9);

                    // Header với màu nền
                    String[] headers = {"Analyte Name", "Value", "Unit", "Reference Range", "Flag", "Measured At"};
                    for (String header : headers) {
                        resultsTable.addHeaderCell(new Cell().add(new Paragraph(header))
                                .setFont(fontBold)
                                .setBackgroundColor(HEADER_BG_COLOR)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setPadding(4));
                    }

                    boolean alternate = false;
                    for (TestResult result : results) {
                        Color rowColor = alternate ? new DeviceRgb(250, 250, 250) : ColorConstants.WHITE;

                        // Kiểm tra bất thường để tô màu đỏ
                        boolean isAbnormal = result.getAbnormalFlag() != null && result.getAbnormalFlag() != AbnormalFlag.N;
                        Color textColor = isAbnormal ? ALERT_COLOR : ColorConstants.BLACK;
                        PdfFont textFont = isAbnormal ? fontBold : fontRegular;

                        // Add cells
                        addResultCell(resultsTable, result.getAnalyteName(), TextAlignment.LEFT, rowColor, fontRegular, ColorConstants.BLACK);
                        addResultCell(resultsTable, result.getValueText(), TextAlignment.CENTER, rowColor, textFont, textColor);
                        addResultCell(resultsTable, result.getUnit(), TextAlignment.CENTER, rowColor, fontRegular, ColorConstants.BLACK);
                        addResultCell(resultsTable, result.getReferenceRange(), TextAlignment.CENTER, rowColor, fontRegular, ColorConstants.BLACK);

                        String flag = result.getAbnormalFlag() != null ? result.getAbnormalFlag().name() : "";
                        addResultCell(resultsTable, flag, TextAlignment.CENTER, rowColor, fontBold, textColor);

                        String time = result.getMeasuredAt() != null ? result.getMeasuredAt().format(DATETIME_FORMATTER) : "";
                        addResultCell(resultsTable, time, TextAlignment.RIGHT, rowColor, fontRegular, ColorConstants.BLACK);

                        alternate = !alternate;
                    }
                    document.add(resultsTable);
                } else {
                    document.add(new Paragraph("No test results available.").setFontSize(10).setItalic());
                }
            } catch (Exception e) {
                log.error("Error adding Table 2", e);
                throw e;
            }

            // 6. COMMENTS (Format khung viền)
            try {
                document.add(new Paragraph("\nIII. GHI CHÚ (COMMENTS)")
                        .setFont(fontBold).setFontSize(11).setFontColor(PRIMARY_COLOR).setMarginBottom(5));

                if (comments != null && !comments.isEmpty()) {
                    Div commentContainer = new Div()
                            .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                            .setPadding(5)
                            .setBackgroundColor(new DeviceRgb(252, 252, 252));

                    addCommentsToContainer(commentContainer, comments, 0, fontRegular, fontBold);
                    document.add(commentContainer);
                } else {
                    document.add(new Paragraph("No comments found.").setFontSize(10).setItalic());
                }
            } catch (Exception e) {
                log.error("Error adding Comments", e);
                throw e;
            }

            // 7. CHỮ KÝ (Không điền tên sẵn)
            addSignatureSection(document, fontBold);

        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw new RuntimeException("Error generating PDF", e);
        }

        return baos.toByteArray();
    }

    // --- HELPER METHODS ---

    private void addClinicHeader(Document document, PdfFont fontBold) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 4})).useAllAvailableWidth();
        headerTable.setBorder(Border.NO_BORDER);

        // Logo giả lập (Màu nền xanh, chữ trắng)
        Cell logoCell = new Cell().add(new Paragraph("LMS")
                .setFont(fontBold).setFontSize(24).setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER));
        logoCell.setBackgroundColor(PRIMARY_COLOR)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setHeight(50);
        headerTable.addCell(logoCell);

        // Thông tin phòng khám
        Paragraph info = new Paragraph()
                .add(new Text("HỆ THỐNG QUẢN LÝ XÉT NGHIỆM\n").setFont(fontBold).setFontSize(12).setFontColor(PRIMARY_COLOR))
                .add(new Text("Địa chỉ: 12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM\n").setFontSize(9))
                .add(new Text("Hotline: 1900 1234").setFontSize(9));

        Cell infoCell = new Cell().add(info)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(10);
        headerTable.addCell(infoCell);

        document.add(headerTable);
    }

    // Helper add row cho bảng thông tin Order (Có border mờ hoặc không border tùy ý, ở đây dùng border mỏng)
    private void addStyledRow(Table table, String label, String value, PdfFont fontLabel, PdfFont fontValue) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(fontLabel))
                .setBackgroundColor(new DeviceRgb(248, 248, 248)) // Nền nhẹ cho nhãn
                .setPadding(3).setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)));

        table.addCell(new Cell().add(new Paragraph(value).setFont(fontValue))
                .setPadding(3).setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)));
    }

    // Helper add cell cho bảng kết quả
    private void addResultCell(Table table, String text, TextAlignment align, Color bgColor, PdfFont font, Color textColor) {
        table.addCell(new Cell().add(new Paragraph(getValueOrNA(text)).setFont(font).setFontColor(textColor))
                .setBackgroundColor(bgColor)
                .setTextAlignment(align)
                .setPadding(4)
                .setVerticalAlignment(VerticalAlignment.MIDDLE));
    }

    // Hàm đệ quy in comment vào container (Div) thay vì in trực tiếp ra Document để đóng khung
    private void addCommentsToContainer(Div container, List<CommentOrderResponse> comments, int level, PdfFont fontRegular, PdfFont fontBold) {
        if (comments == null || comments.isEmpty()) return;

        // Cấu hình thụt lề
        float levelIndent = level * 20f; // Thụt đầu dòng theo cấp độ cha/con
        float bulletWidth = 15f;         // Khoảng cách dành riêng cho icon đầu dòng (để tạo hiệu ứng treo)

        for (CommentOrderResponse comment : comments) {
            String authorName = (comment.getAuthor() != null) ? getValueOrNA(comment.getAuthor().getFullName()) : "N/A";
            String time = (comment.getCreatedAt() != null) ? comment.getCreatedAt().format(DATETIME_FORMATTER) : "";

            // LOGIC HANGING INDENT:
            // setMarginLeft(X): Đẩy toàn bộ đoạn văn sang phải X.
            // setFirstLineIndent(-Y): Kéo riêng dòng đầu tiên (chứa icon) lùi lại Y.
            // Kết quả: Icon nằm bên trái, toàn bộ khối văn bản (tên + nội dung) thẳng hàng bên phải.
            Paragraph p = new Paragraph()
                    .setMarginLeft(levelIndent + bulletWidth)
                    .setFirstLineIndent(-bulletWidth)
                    .setMarginBottom(4)
                    .setFontSize(9)
                    .setFont(fontRegular);

            // 1. Icon đầu dòng (Prefix)
            String prefixSymbol = (level == 0) ? "• " : "> ";
            Text prefix = new Text(prefixSymbol)
                    .setFont(fontRegular)
                    .setFontColor(ColorConstants.GRAY) // Màu xám nhạt để không rối mắt
                    .setFontSize(level == 0 ? 10 : 12); // Reply icon to hơn một chút cho rõ

            p.add(prefix);

            // 2. Tên người viết (Màu xanh thương hiệu)
            p.add(new Text(authorName).setFont(fontBold).setFontColor(PRIMARY_COLOR));

            // 3. Thời gian (Nhỏ, xám)
            p.add(new Text(" [" + time + "]: ").setFontSize(8).setFontColor(ColorConstants.GRAY));

            // 4. Nội dung comment (Màu đen mặc định)
            p.add(new Text(getValueOrNA(comment.getContent())).setFont(fontRegular));

            // 5. Thông tin tham chiếu (nếu có)
            if (comment.getTargetInfo() != null && comment.getTargetInfo().getTargetType() == fit.test_order_service.enums.CommentTargetType.RESULT) {
                p.add(new Text(String.format(" (Ref: %s)", comment.getTargetInfo().getAnalyteName()))
                        .setFontSize(8).setItalic().setFontColor(ColorConstants.GRAY));
            }

            container.add(p);

            // Đệ quy cho comment con
            addCommentsToContainer(container, comment.getReplies(), level + 1, fontRegular, fontBold);
        }
    }

    private void addSignatureSection(Document document, PdfFont fontBold) {
        document.add(new Paragraph("\n\n"));
        Table signTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
        signTable.setBorder(Border.NO_BORDER);

        // Bên trái để trống
        signTable.addCell(new Cell().setBorder(Border.NO_BORDER));

        // Bên phải
        Paragraph signBlock = new Paragraph()
                .add(new Text("Ngày ..... tháng ..... năm .....\n").setItalic().setFontSize(10))
                .add(new Text("KỸ THUẬT VIÊN / BÁC SĨ\n").setFont(fontBold).setFontSize(11))
                .add(new Text("(Ký và ghi rõ họ tên)\n\n\n\n\n")) // Thêm dòng trống để ký
                //.add(new Text("Nguyen Van A")) // <--- ĐÃ BỎ TÊN SẴN THEO YÊU CẦU
                .setTextAlignment(TextAlignment.CENTER);

        signTable.addCell(new Cell().add(signBlock).setBorder(Border.NO_BORDER));
        document.add(signTable);
    }

    private PdfFont loadFont(String path, String fallback) {
        try {
            return PdfFontFactory.createFont(path, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        } catch (IOException e) {
            log.warn("Font error: {}. Using fallback.", e.getMessage());
            try {
                return PdfFontFactory.createFont(fallback);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private String getValueOrNA(String value) {
        return (value != null && !value.isBlank()) ? value : "N/A";
    }

    private String getUserFullName(String userId) {
        if (userId == null || userId.isBlank() || "SYSTEM".equals(userId)) {
            return userId != null ? userId : "N/A";
        }
        try {
            ApiResponse<UserInternalResponse> response = iamFeignClient.getUserById(userId);
            if (response != null && response.getData() != null) {
                return response.getData().fullName();
            }
        } catch (Exception e) {
            // log.warn("Error getting user name", e); // Giảm log noise
        }
        return userId;
    }
}