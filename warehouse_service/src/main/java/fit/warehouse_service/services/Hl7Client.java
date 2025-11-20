/*
 * @ {#} Hl7SocketClient.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.services;

import fit.warehouse_service.entities.Instrument;
import fit.warehouse_service.enums.InstrumentStatus;
import fit.warehouse_service.enums.ProtocolType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * @description: HL7 Client for communicating with instruments via HL7 protocol
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
@Component
@Slf4j
public class Hl7Client {
    @Value("${app.hl7.mock-enabled:true}")
    private boolean mockEnabled;


    /**
     * Gửi yêu cầu “check status” đến máy và nhận phản hồi HL7.
     *
     * @param instrument thiết bị cần kiểm tra
     * @return HL7 message phản hồi từ máy
     */

    public String requestStatus(Instrument instrument) {
        if (instrument.getProtocolType() != ProtocolType.HL7) {
            throw new UnsupportedOperationException("Only HL7 protocol is supported for now");
        }

        if (mockEnabled) {
            String queryMessage = buildStatusQueryMessage(instrument);
            log.info("[MOCK MODE] --- Sending HL7 Query (simulated) ---", queryMessage);

            // Giả lập phản hồi từ máy (ORU^R01)
            String response = generateMockResponse(instrument);
            log.info("[MOCK MODE] --- HL7 Response (simulated) ---\n{}", response);

            return response;
        }

        log.warn("HL7 real socket communication not implemented yet. Returning mock response.");
        return generateMockResponse(instrument);
    }

    /**
     * Sinh ra message HL7 mẫu (giả lập phản hồi từ thiết bị thật)
     */
    private String generateMockResponse(Instrument instrument) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String messageId = "MOCK_" + System.currentTimeMillis();

        // 70% READY, 30% ERROR
        InstrumentStatus status = Math.random() < 0.7 ? InstrumentStatus.READY : InstrumentStatus.ERROR;
        String statusText = status.name();

        String nteSegment = status == InstrumentStatus.ERROR
                ? "NTE|1|L|ERROR CODE 207: Simulated hardware fault\\r"
                : "";

        String mockResponse = String.join("\r",
                "MSH|^~\\\\&|MOCKDEVICE|" + instrument.getName() + "|LIS|BV|" + timestamp + "||ORU^R01|" + messageId + "|P|2.5",
                "OBR|1|STAT001|RUN001|STATUS^Instrument Status",
                "OBX|1|ST|STATUS^Instrument Status||" + statusText + "|||N|||F",
                nteSegment,
                "L|1|F"
        );
        return mockResponse;
    }

    /**
     * Tạo HL7 QRY message (Yêu cầu trạng thái thiết bị)
     */
    private String buildStatusQueryMessage(Instrument instrument) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String messageId = "STAT_" + System.currentTimeMillis();

        return String.join("\r",
                "MSH|^~\\&|LIS|BV|" + instrument.getName() + "|LAB|" + timestamp + "||QRY^Q02|" + messageId + "|P|2.5",
                "QRD|" + timestamp + "|R|I|Q001|||RES"
        );
    }
}
